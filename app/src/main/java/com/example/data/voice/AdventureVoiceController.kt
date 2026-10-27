package com.example.data.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import com.example.data.model.ActivityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

enum class AdventureGuideMode { WANDER, TOUR }
enum class AdventureVoiceStyle { CALM, BALANCED, ENERGETIC }
enum class VoiceStartRequest { PUSH_TO_TALK, HANDS_FREE }

enum class AdventureVoiceAction {
    IGNORED, WAKE_ONLY, HELP, STOP_SPEAKING, DISABLE_HANDS_FREE, REPEAT_LAST,
    BACK, NEXT_SCREEN, PREVIOUS_SCREEN, OPEN_FEED, OPEN_EXPLORE, OPEN_TRACKER,
    OPEN_VOICE, OPEN_STORY_STUDIO, OPEN_RADAR, OPEN_PROFILE, START_ACTIVITY,
    PAUSE_ACTIVITY, RESUME_ACTIVITY, END_ACTIVITY, ACTIVITY_BRIEFING, RIDE_STATUS,
    SAFETY_HELP, SEARCH_PLACE, NEARBY_CONTEXT, SELECT_ACTIVITY, GUIDED_MODE, WANDER_MODE, UNKNOWN
}

data class ParsedAdventureVoiceCommand(
    val action: AdventureVoiceAction,
    val query: String? = null,
    val activity: ActivityType? = null,
    val wakeDetected: Boolean = false,
)

data class VoiceCommandReply(
    val text: String = "",
    val afterSpeech: (() -> Unit)? = null,
    val silent: Boolean = false,
)

/** A deliberately conservative motion signal; GPS speed alone is not used in demo/no-fix mode. */
data class VoiceMotionSnapshot(
    val activityType: ActivityType,
    val isRecording: Boolean,
    val isPaused: Boolean,
    val hasGpsFix: Boolean,
    val isDemo: Boolean,
    val speedKmH: Double,
)

object AdventureVoiceSafetyPolicy {
    /**
     * Restricts long spoken briefings and app-switching place searches only when the app has a
     * confirmed live GPS fix for a moving motorcycle/car session. Walking and demo telemetry do
     * not trigger this guard.
     */
    fun isVehicleInMotion(snapshot: VoiceMotionSnapshot): Boolean =
        snapshot.isRecording &&
            !snapshot.isPaused &&
            snapshot.hasGpsFix &&
            !snapshot.isDemo &&
            snapshot.activityType in setOf(ActivityType.Motorcycling, ActivityType.CarTrips) &&
            snapshot.speedKmH >= 5.0
}

data class AdventureVoiceState(
    val handsFreeEnabled: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val awaitingCommand: Boolean = false,
    val isReady: Boolean = false,
    val isOnDeviceRecognition: Boolean = false,
    val hasLocalTtsVoice: Boolean = false,
    val guideMode: AdventureGuideMode = AdventureGuideMode.WANDER,
    val voiceStyle: AdventureVoiceStyle = AdventureVoiceStyle.BALANCED,
    val languageCode: String = "en",
    val status: String = "Ready. Tap the microphone or enable hands-free mode.",
    val lastTranscript: String = "",
    val lastResponse: String = "",
)

/**
 * Explicitly user-armed, foreground voice control for Adventurer.
 *
 * This is not an OS-level hotword service: Android's standard SpeechRecognizer is not intended
 * for endless background listening. Listening pauses when the Activity leaves the foreground.
 */
class AdventureVoiceController(private val context: Context) {
    private val prefs = context.getSharedPreferences("adventurer_voice_preferences", Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsReady = false
    private var foregroundActive = false
    private var currentUtteranceId: String? = null
    private var pendingAfterSpeech: (() -> Unit)? = null
    private var commandHandler: ((ParsedAdventureVoiceCommand) -> VoiceCommandReply)? = null

    private val _state = MutableStateFlow(
        AdventureVoiceState(
            guideMode = runCatching {
                AdventureGuideMode.valueOf(prefs.getString("guide_mode", AdventureGuideMode.WANDER.name)!!)
            }.getOrDefault(AdventureGuideMode.WANDER),
            voiceStyle = runCatching {
                AdventureVoiceStyle.valueOf(prefs.getString("voice_style", AdventureVoiceStyle.BALANCED.name)!!)
            }.getOrDefault(AdventureVoiceStyle.BALANCED),
        )
    )
    val state: StateFlow<AdventureVoiceState> = _state.asStateFlow()

    init {
        // Use the Activity context for SpeechRecognizer; callers shut the controller down with
        // the composition so it cannot retain the Activity after it is destroyed.
        textToSpeech = TextToSpeech(context.applicationContext) { result ->
            mainHandler.post {
                if (result == TextToSpeech.SUCCESS) {
                    ttsReady = true
                    configureTtsVoice()
                    _state.value = _state.value.copy(
                        isReady = true,
                        status = "Voice ready. Say “Hey Adventurer” after enabling hands-free mode.",
                    )
                    attachTtsListener()
                } else {
                    _state.value = _state.value.copy(
                        isReady = false,
                        status = "The device's text-to-speech engine could not start.",
                    )
                }
            }
        }
    }

    fun setCommandHandler(handler: (ParsedAdventureVoiceCommand) -> VoiceCommandReply) {
        commandHandler = handler
    }

    fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun updateLanguage(code: String) {
        _state.value = _state.value.copy(languageCode = code.ifBlank { "en" })
        prefs.edit().putString("language_code", code).apply()
        if (ttsReady) configureTtsVoice()
    }

    fun setGuideMode(mode: AdventureGuideMode) {
        _state.value = _state.value.copy(guideMode = mode)
        prefs.edit().putString("guide_mode", mode.name).apply()
    }

    fun setVoiceStyle(style: AdventureVoiceStyle) {
        _state.value = _state.value.copy(voiceStyle = style)
        prefs.edit().putString("voice_style", style.name).apply()
    }

    fun permissionDenied() {
        _state.value = _state.value.copy(
            isListening = false,
            status = "Microphone permission is needed for voice control. You can still use Adventurer normally.",
        )
    }

    fun enableHandsFree() {
        if (!hasMicrophonePermission()) {
            permissionDenied()
            return
        }
        _state.value = _state.value.copy(
            handsFreeEnabled = true,
            awaitingCommand = false,
            status = if (foregroundActive) "Hands-free enabled. Listening for “Hey Adventurer” on this screen." else "Hands-free will resume when Adventurer is in the foreground.",
        )
        if (foregroundActive) startListening()
    }

    fun disableHandsFree() {
        _state.value = _state.value.copy(handsFreeEnabled = false, awaitingCommand = false, isListening = false)
        stopRecognizer()
        _state.value = _state.value.copy(status = "Hands-free listening is off.")
    }

    fun startOneShot() {
        if (!hasMicrophonePermission()) {
            permissionDenied()
            return
        }
        stopSpeaking()
        stopRecognizer()
        _state.value = _state.value.copy(
            awaitingCommand = true,
            isListening = false,
            status = "Listening for a command. Try “open Explore” or “go back”.",
        )
        if (foregroundActive) startListening()
    }

    fun setForegroundActive(active: Boolean) {
        foregroundActive = active
        if (!active) {
            _state.value = _state.value.copy(isListening = false)
            stopRecognizer()
            if (_state.value.handsFreeEnabled) {
                _state.value = _state.value.copy(status = "Listening paused while the app is in the background.")
            }
        } else if (_state.value.handsFreeEnabled && hasMicrophonePermission() && !_state.value.isSpeaking) {
            startListening()
        }
    }

    fun say(text: String, afterSpeech: (() -> Unit)? = null) {
        if (text.isBlank()) {
            afterSpeech?.invoke()
            return
        }
        pendingAfterSpeech = afterSpeech
        val utteranceId = UUID.randomUUID().toString()
        currentUtteranceId = utteranceId
        _state.value = _state.value.copy(
            lastResponse = text,
            isSpeaking = true,
            isListening = false,
            status = "Speaking. Tap Stop to interrupt.",
        )
        stopRecognizer()
        val engine = textToSpeech
        if (!ttsReady || engine == null) {
            _state.value = _state.value.copy(
                isSpeaking = false,
                status = "Text-to-speech is not ready. Check the speech engine installed on this device.",
            )
            pendingAfterSpeech?.invoke()
            pendingAfterSpeech = null
            return
        }
        configureTtsVoice()
        when (_state.value.voiceStyle) {
            AdventureVoiceStyle.CALM -> { engine.setSpeechRate(0.88f); engine.setPitch(0.94f) }
            AdventureVoiceStyle.BALANCED -> { engine.setSpeechRate(1.0f); engine.setPitch(1.0f) }
            AdventureVoiceStyle.ENERGETIC -> { engine.setSpeechRate(1.06f); engine.setPitch(1.06f) }
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        currentUtteranceId = null
        pendingAfterSpeech = null
        _state.value = _state.value.copy(isSpeaking = false, status = "Speech stopped.")
        if (_state.value.handsFreeEnabled && foregroundActive) scheduleRestart(250)
    }

    fun shutdown() {
        mainHandler.removeCallbacksAndMessages(null)
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        foregroundActive = false
    }

    private fun configureTtsVoice() {
        val engine = textToSpeech ?: return
        val locale = Locale.forLanguageTag(_state.value.languageCode)
        runCatching { engine.setLanguage(locale) }
        val localVoice = runCatching {
            engine.voices
                ?.filter { it.locale.language == locale.language && !it.isNetworkConnectionRequired }
                ?.maxByOrNull { it.quality }
        }.getOrNull()
        if (localVoice != null) runCatching { engine.voice = localVoice }
        _state.value = _state.value.copy(hasLocalTtsVoice = localVoice != null)
    }

    private fun attachTtsListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId == currentUtteranceId) {
                        _state.value = _state.value.copy(isSpeaking = true)
                    }
                }
            }

            override fun onDone(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId != currentUtteranceId) return@post
                    currentUtteranceId = null
                    _state.value = _state.value.copy(isSpeaking = false, status = "Ready.")
                    val followUp = pendingAfterSpeech
                    pendingAfterSpeech = null
                    followUp?.invoke()
                    if (_state.value.handsFreeEnabled && foregroundActive && !_state.value.isSpeaking) {
                        scheduleRestart(250)
                    }
                }
            }

            @Deprecated("Deprecated by Android API; retained for engines that call the legacy overload.")
            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    if (utteranceId == currentUtteranceId) {
                        currentUtteranceId = null
                        _state.value = _state.value.copy(isSpeaking = false, status = "Speech playback failed.")
                        pendingAfterSpeech = null
                        if (_state.value.handsFreeEnabled && foregroundActive) scheduleRestart(400)
                    }
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) = onError(utteranceId)
        })
    }

    private fun ensureRecognizer() {
        if (speechRecognizer != null) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = _state.value.copy(status = "No speech-recognition service is available on this device.")
            return
        }
        val onDeviceAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            runCatching { SpeechRecognizer.isOnDeviceRecognitionAvailable(context) }.getOrDefault(false)
        speechRecognizer = runCatching {
            if (onDeviceAvailable) SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            else SpeechRecognizer.createSpeechRecognizer(context)
        }.getOrElse {
            runCatching { SpeechRecognizer.createSpeechRecognizer(context) }.getOrNull()
        }
        _state.value = _state.value.copy(isOnDeviceRecognition = onDeviceAvailable)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                mainHandler.post {
                    _state.value = _state.value.copy(
                        isListening = true,
                        status = if (_state.value.handsFreeEnabled && !_state.value.awaitingCommand)
                            "Listening for “Hey Adventurer”…"
                        else "Listening for your command…",
                    )
                }
            }

            override fun onBeginningOfSpeech() {
                mainHandler.post { _state.value = _state.value.copy(status = "Listening…") }
            }

            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                mainHandler.post { _state.value = _state.value.copy(isListening = false, status = "Thinking…") }
            }

            override fun onError(error: Int) {
                mainHandler.post {
                    _state.value = _state.value.copy(isListening = false)
                    if (_state.value.isSpeaking) return@post
                    if (_state.value.handsFreeEnabled && foregroundActive) {
                        _state.value = _state.value.copy(status = "Still listening for “Hey Adventurer”…")
                        scheduleRestart(if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 900 else 500)
                    } else {
                        _state.value = _state.value.copy(
                            awaitingCommand = false,
                            status = when (error) {
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is needed."
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition needs a network or an available offline language pack."
                                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap the microphone to try again."
                                else -> "Speech recognition stopped. Tap the microphone to try again."
                            },
                        )
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                mainHandler.post { processTranscript(text) }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) mainHandler.post {
                    _state.value = _state.value.copy(lastTranscript = text)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    private fun startListening() {
        if (!foregroundActive || _state.value.isSpeaking || !hasMicrophonePermission()) return
        ensureRecognizer()
        val recognizer = speechRecognizer ?: return
        mainHandler.removeCallbacks(restartListeningRunnable)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, _state.value.languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, _state.value.languageCode)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        runCatching {
            _state.value = _state.value.copy(isListening = true)
            recognizer.startListening(intent)
        }.onFailure {
            _state.value = _state.value.copy(isListening = false, status = "Could not start listening on this device.")
            if (_state.value.handsFreeEnabled) scheduleRestart(900)
        }
    }

    private val restartListeningRunnable = Runnable { startListening() }

    private fun scheduleRestart(delayMillis: Long) {
        mainHandler.removeCallbacks(restartListeningRunnable)
        if (_state.value.handsFreeEnabled && foregroundActive && !_state.value.isSpeaking) {
            mainHandler.postDelayed(restartListeningRunnable, delayMillis)
        }
    }

    private fun stopRecognizer() {
        _state.value = _state.value.copy(isListening = false)
        runCatching { speechRecognizer?.cancel() }
    }

    private fun processTranscript(rawText: String) {
        _state.value = _state.value.copy(isListening = false)
        val text = rawText.trim()
        if (text.isBlank()) {
            if (_state.value.handsFreeEnabled && foregroundActive) scheduleRestart(350)
            else _state.value = _state.value.copy(awaitingCommand = false, status = "I didn't catch that. Tap the microphone and try again.")
            return
        }
        val shouldRequireWake = _state.value.handsFreeEnabled && !_state.value.awaitingCommand
        val parsed = AdventureVoiceCommandParser.parse(text, requireWakePhrase = shouldRequireWake)
        if (parsed.action == AdventureVoiceAction.IGNORED) {
            if (_state.value.handsFreeEnabled && foregroundActive) scheduleRestart(350)
            return
        }
        _state.value = _state.value.copy(lastTranscript = text, awaitingCommand = false)
        when (parsed.action) {
            AdventureVoiceAction.WAKE_ONLY -> {
                _state.value = _state.value.copy(awaitingCommand = true)
                say("I'm listening. What do you need?", null)
            }
            AdventureVoiceAction.STOP_SPEAKING -> stopSpeaking()
            AdventureVoiceAction.DISABLE_HANDS_FREE -> {
                disableHandsFree()
                say("Hands-free listening is off.")
            }
            AdventureVoiceAction.REPEAT_LAST -> {
                val previous = _state.value.lastResponse
                if (previous.isBlank()) say("I haven't said anything to repeat yet.")
                else say(previous)
            }
            AdventureVoiceAction.HELP -> {
                val reply = commandHandler?.invoke(parsed)
                    ?: VoiceCommandReply("Try “open Explore”, “go back”, “give me an activity briefing”, or “search for a place”.")
                say(reply.text, reply.afterSpeech)
            }
            AdventureVoiceAction.GUIDED_MODE -> {
                setGuideMode(AdventureGuideMode.TOUR)
                say("Tour mode selected. Ask for an activity briefing whenever you want a longer guided summary.")
            }
            AdventureVoiceAction.WANDER_MODE -> {
                setGuideMode(AdventureGuideMode.WANDER)
                say("Wander mode selected. I'll keep replies short and wait for you to ask.")
            }
            else -> {
                val reply = commandHandler?.invoke(parsed)
                    ?: VoiceCommandReply("Voice controls are ready, but no command handler is connected.")
                if (reply.silent) {
                    reply.afterSpeech?.invoke()
                    _state.value = _state.value.copy(status = "Ready.")
                    if (_state.value.handsFreeEnabled && foregroundActive) scheduleRestart(250)
                } else say(reply.text, reply.afterSpeech)
            }
        }
    }

    companion object {
        fun isPermissionGranted(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }
}

/** Small, deterministic command vocabulary. It does not ask a language model to invent actions. */
object AdventureVoiceCommandParser {
    private val wakePhrases = listOf("hey adventurer", "hi adventurer", "okay adventurer", "ok adventurer")

    fun parse(transcript: String, requireWakePhrase: Boolean = false): ParsedAdventureVoiceCommand {
        val normalized = transcript.lowercase(Locale.ROOT)
            .replace(Regex("(?<=\\p{L})['’](?=\\p{L})"), "")
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (normalized.isBlank()) return ParsedAdventureVoiceCommand(AdventureVoiceAction.IGNORED)
        val wake = wakePhrases.firstOrNull { normalized == it || normalized.startsWith("$it ") }
        if (requireWakePhrase && wake == null) return ParsedAdventureVoiceCommand(AdventureVoiceAction.IGNORED)
        val phrase = if (wake != null) normalized.removePrefix(wake).trim() else normalized
        if (phrase.isBlank()) return ParsedAdventureVoiceCommand(AdventureVoiceAction.WAKE_ONLY, wakeDetected = wake != null)

        val exact = phrase.trim()
        fun result(action: AdventureVoiceAction, query: String? = null, activity: ActivityType? = null) =
            ParsedAdventureVoiceCommand(action, query = query, activity = activity, wakeDetected = wake != null)

        when {
            exact in setOf("stop talking", "stop speaking", "cancel speech", "be quiet", "silence") ->
                return result(AdventureVoiceAction.STOP_SPEAKING)
            exact in setOf("stop listening", "turn off hands free", "turn off hands-free", "disable hands free", "disable hands-free") ->
                return result(AdventureVoiceAction.DISABLE_HANDS_FREE)
            exact in setOf("repeat", "repeat that", "say that again", "repeat last") -> return result(AdventureVoiceAction.REPEAT_LAST)
            exact in setOf("help", "what can you do", "voice help", "commands") -> return result(AdventureVoiceAction.HELP)
            exact in setOf("back", "go back", "previous screen", "go to previous screen") -> return result(AdventureVoiceAction.BACK)
            exact in setOf("next", "next screen", "go forward", "go to next screen") -> return result(AdventureVoiceAction.NEXT_SCREEN)
            exact in setOf("previous", "previous tab", "go to previous tab") -> return result(AdventureVoiceAction.PREVIOUS_SCREEN)
            exact in setOf("home", "go home", "open home", "open feed", "show feed") -> return result(AdventureVoiceAction.OPEN_FEED)
            exact in setOf("explore", "open explore", "open explore and plan", "open planner", "plan a trip") -> return result(AdventureVoiceAction.OPEN_EXPLORE)
            exact in setOf("open tracker", "open ride", "open activity", "show tracker", "open live tracker") -> return result(AdventureVoiceAction.OPEN_TRACKER)
            exact in setOf("open voice", "open voice companion", "open assistant") -> return result(AdventureVoiceAction.OPEN_VOICE)
            exact in setOf("open story studio", "create a story", "open story", "make a story") -> return result(AdventureVoiceAction.OPEN_STORY_STUDIO)
            exact in setOf("open safety", "open help", "open radar", "open roadside radar") -> return result(AdventureVoiceAction.OPEN_RADAR)
            exact in setOf("open profile", "show profile", "open my profile") -> return result(AdventureVoiceAction.OPEN_PROFILE)
            exact in setOf("start recording", "start activity", "start trip", "begin activity", "begin ride") -> return result(AdventureVoiceAction.START_ACTIVITY)
            exact in setOf("pause", "pause recording", "pause activity", "pause trip", "pause ride") -> return result(AdventureVoiceAction.PAUSE_ACTIVITY)
            exact in setOf("resume", "resume recording", "resume activity", "resume trip", "resume ride", "continue trip") -> return result(AdventureVoiceAction.RESUME_ACTIVITY)
            exact in setOf("end trip", "finish trip", "stop activity", "open trip controls") -> return result(AdventureVoiceAction.END_ACTIVITY)
            exact in setOf("activity briefing", "give me an activity briefing", "guide me", "what should i know", "tell me about this activity") -> return result(AdventureVoiceAction.ACTIVITY_BRIEFING)
            exact in setOf("status", "trip status", "activity status", "how far have i gone", "how far", "current speed", "read my status") -> return result(AdventureVoiceAction.RIDE_STATUS)
            exact in setOf("sos", "emergency", "i need help", "safety help") -> return result(AdventureVoiceAction.SAFETY_HELP)
            exact in setOf("guided tour", "tour mode", "switch to tour mode") -> return result(AdventureVoiceAction.GUIDED_MODE)
            exact in setOf("wander mode", "switch to wander mode", "on demand mode") -> return result(AdventureVoiceAction.WANDER_MODE)
            exact in setOf("what is that", "whats that", "what's that", "what is this", "whats this", "what's this", "tell me about nearby places", "what is around me") -> return result(AdventureVoiceAction.NEARBY_CONTEXT)
        }

        val activityPrefix = listOf("switch activity to ", "change activity to ", "switch to ", "use activity ")
            .firstOrNull { exact.startsWith(it) }
        if (activityPrefix != null) {
            val activityText = exact.removePrefix(activityPrefix).trim()
            val activity = parseActivity(activityText)
            if (activity != null) return result(AdventureVoiceAction.SELECT_ACTIVITY, activity = activity)
        }

        val searchPrefix = listOf("search for ", "look up ", "find place ", "tell me about ", "where is ")
            .firstOrNull { exact.startsWith(it) }
        if (searchPrefix != null) {
            val query = exact.removePrefix(searchPrefix).trim()
            if (query.isNotBlank() && query !in setOf("that", "this", "it", "there", "nearby places")) {
                return result(AdventureVoiceAction.SEARCH_PLACE, query = query)
            }
        }
        return result(AdventureVoiceAction.UNKNOWN)
    }

    private fun parseActivity(text: String): ActivityType? = when {
        "motorcycle" in text || "motorbike" in text || "motorcycling" in text -> ActivityType.Motorcycling
        "cycling" in text || "bikepacking" in text || text == "biking" -> ActivityType.Cycling
        "trek" in text || "hiking" in text -> ActivityType.Trekking
        "walking" in text || text == "walk" -> ActivityType.Walking
        "car" in text || "road trip" in text || "driving" in text -> ActivityType.CarTrips
        "camp" in text || "overland" in text -> ActivityType.Camping
        "fish" in text || "angling" in text -> ActivityType.Fishing
        "hunt" in text -> ActivityType.Hunting
        "wildlife" in text || "nature" in text -> ActivityType.WildlifeExploration
        "scuba" in text || "dive" in text -> ActivityType.ScubaDiving
        "snorkel" in text -> ActivityType.Snorkeling
        "kayak" in text || "paddl" in text -> ActivityType.KayakingPaddling
        "sail" in text || "boat" in text -> ActivityType.BoatingSailing
        "island" in text || "coast" in text -> ActivityType.IslandExploration
        "cave" in text || "climb" in text -> ActivityType.ClimbingCaving
        "explor" in text || "new place" in text -> ActivityType.DestinationExploration
        else -> null
    }
}
