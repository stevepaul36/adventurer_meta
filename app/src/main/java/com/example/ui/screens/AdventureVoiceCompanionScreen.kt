package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.voice.AdventureGuideMode
import com.example.data.voice.AdventureGuidePreferenceOptions
import com.example.data.voice.AdventureGuidePreferences
import com.example.data.voice.AdventureGuidePreferencesStore
import com.example.data.voice.AdventureRealtimeVoiceSession
import com.example.data.voice.AdventureVoiceController
import com.example.data.voice.AdventureVoiceStyle
import com.example.data.voice.VoiceStartRequest
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.viewmodel.AdventurerViewModel

@Composable
fun AdventureVoiceCompanionScreen(
    viewModel: AdventurerViewModel,
    controller: AdventureVoiceController,
    liveGuideSession: AdventureRealtimeVoiceSession,
    preferencesStore: AdventureGuidePreferencesStore,
    onGuidePreferencesChanged: (AdventureGuidePreferences) -> Unit,
    onRequestVoice: (VoiceStartRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()
    val liveState by liveGuideSession.state.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
    var guidePreferences by remember(preferencesStore) { mutableStateOf(preferencesStore.load()) }
    var showPersonalization by rememberSaveable { mutableStateOf(false) }
    var chatInput by rememberSaveable { mutableStateOf("") }
    var lastLiveTranscript by remember { mutableStateOf("") }
    var lastLiveUserTranscriptItemId by remember { mutableStateOf("") }
    val chatLines = remember { mutableStateListOf<VoiceChatLine>() }

    LaunchedEffect(liveState.incomingSpeechTurnSequence) {
        val sequence = liveState.incomingSpeechTurnSequence
        val id = "voice-$sequence"
        if (sequence > 0 && chatLines.none { it.id == id }) {
            chatLines.add(VoiceChatLine(isUser = true, text = "Listening…", id = id))
            while (chatLines.size > 12) chatLines.removeAt(0)
        }
    }

    LaunchedEffect(liveState.interimUserTranscriptSequence, liveState.interimUserTranscript) {
        val sequence = liveState.interimUserTranscriptSequence
        val partial = liveState.interimUserTranscript.trim()
        val id = "voice-$sequence"
        if (sequence > 0) {
            val index = chatLines.indexOfLast { it.id == id }
            if (index >= 0 && partial.isNotBlank()) chatLines[index] = chatLines[index].copy(text = partial)
        }
    }

    LaunchedEffect(liveState.latestUserTranscriptItemId) {
        val itemId = liveState.latestUserTranscriptItemId
        val transcript = liveState.latestUserTranscript.trim()
        val id = "voice-" + liveState.latestUserTranscriptSequence
        if (itemId.isNotBlank() && itemId != lastLiveUserTranscriptItemId && transcript.isNotBlank()) {
            val index = chatLines.indexOfLast { it.id == id }
            if (index >= 0) chatLines[index] = chatLines[index].copy(text = transcript)
            else chatLines.add(VoiceChatLine(isUser = true, text = transcript, id = id))
            lastLiveUserTranscriptItemId = itemId
            while (chatLines.size > 12) chatLines.removeAt(0)
        }
    }

    // Stream partial audio transcripts in the status panel; append only once the response finishes.
    LaunchedEffect(liveState.isSpeaking, liveState.latestTranscript) {
        val transcript = liveState.latestTranscript.trim()
        if (!liveState.isSpeaking && transcript.isNotBlank() && transcript != lastLiveTranscript) {
            chatLines.add(VoiceChatLine(isUser = false, text = transcript))
            lastLiveTranscript = transcript
            while (chatLines.size > 12) chatLines.removeAt(0)
        }
    }

    fun sendTypedMessage() {
        val message = chatInput.trim()
        if (message.isBlank()) return
        chatLines.add(VoiceChatLine(isUser = true, text = message))
        while (chatLines.size > 12) chatLines.removeAt(0)
        chatInput = ""
        if (liveState.isConnected) {
            lastLiveTranscript = ""
            if (!liveGuideSession.sendTextTurn(message)) {
                chatLines.add(VoiceChatLine(isUser = false, text = "Live Guide couldn't queue that message. Wait for the current reply or reconnect, then try again."))
            }
        } else {
            val reply = controller.submitTextCommand(message, speakResponse = false)
            chatLines.add(VoiceChatLine(isUser = false, text = reply.ifBlank { "Command received." }))
        }
        while (chatLines.size > 12) chatLines.removeAt(0)
    }

    fun saveGuidePreferences(updated: AdventureGuidePreferences) {
        val saved = preferencesStore.save(updated)
        guidePreferences = saved
        onGuidePreferencesChanged(saved)
    }
    val telemetry by viewModel.telemetry.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Adventure Voice Companion", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Tap to talk or type a command. Connect Live AI Guide for open-ended conversation; preferences are optional and can be changed later.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            color = if (state.handsFreeEnabled) AdventureOrange else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                        ) {
                            Icon(
                                if (state.isSpeaking) Icons.Filled.VolumeUp else Icons.Filled.Mic,
                                contentDescription = null,
                                tint = if (state.handsFreeEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                when {
                                    state.isSpeaking -> "Adventurer is speaking"
                                    state.isListening -> "Listening"
                                    state.handsFreeEnabled -> "Hey Adventurer is armed"
                                    !controller.isSpeechRecognitionAvailable() -> "Speech input unavailable · type below"
                                    !state.isReady -> "Starting voice output…"
                                    else -> "Ready to talk or type"
                                },
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                if (!controller.isSpeechRecognitionAvailable()) "${state.status} You can still use the text command box below."
                                else state.status,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { onRequestVoice(VoiceStartRequest.PUSH_TO_TALK) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Mic, contentDescription = null)
                            Spacer(Modifier.width(3.dp))
                            Text("Talk now")
                        }
                        OutlinedButton(onClick = { if (state.isSpeaking) controller.stopSpeaking() else controller.say("I'm Adventurer. You can say open Explore, go back, give me an activity briefing, or read my trip status.") }) {
                            Icon(Icons.Filled.Stop, contentDescription = "Stop or preview help")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (state.handsFreeEnabled) {
                            OutlinedButton(onClick = { controller.disableHandsFree() }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Pause, contentDescription = null)
                                Spacer(Modifier.width(2.dp))
                                Text("Turn off hands-free")
                            }
                        } else {
                            Button(onClick = { onRequestVoice(VoiceStartRequest.HANDS_FREE) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Headphones, contentDescription = null)
                                Spacer(Modifier.padding(horizontal = 2.dp))
                                Text("Enable “Hey Adventurer”")
                            }
                        }
                    }
                    Text(
                        if (state.isOnDeviceRecognition) "Speech recognition: on-device service selected."
                        else "Speech recognition: device-default service. Depending on the device, recognition may need a network connection.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        if (state.hasLocalTtsVoice) "Narration: a local/offline TTS voice is available for the selected language."
                        else "Narration: a local TTS voice is not confirmed. Check Android text-to-speech voice downloads for offline use.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Headphones, contentDescription = null, tint = AdventureOrange)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Live AI Guide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text(
                                when {
                                    liveState.isConnected -> "Connected · full-duplex voice"
                                    liveState.isConnecting -> "Connecting securely…"
                                    else -> "Natural back-and-forth conversation"
                                },
                                color = if (liveState.isConnected) AdventureOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Text(
                        "Speak naturally, interrupt the guide mid-sentence, ask follow-up questions, and ask what might be nearby. Adventurer can prefetch nearby map context at connection time; place identity and direction remain uncertain.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (liveState.isConnected || liveState.isConnecting) {
                        OutlinedButton(
                            onClick = { liveGuideSession.disconnect() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !liveState.isConnecting,
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (liveState.isConnecting) "Connecting…" else "Stop Live Guide")
                        }
                    } else {
                        Button(
                            onClick = { onRequestVoice(VoiceStartRequest.REALTIME_GUIDE) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !liveState.isConnecting,
                        ) {
                            Icon(Icons.Filled.Headphones, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Start Live AI Guide")
                        }
                    }
                    Text(liveState.status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    liveState.lastError?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = AmberGold, style = MaterialTheme.typography.bodySmall)
                    }
                    liveState.interimUserTranscript.takeIf { it.isNotBlank() }?.let { partial ->
                        Text("You: $partial…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    if (liveState.latestTranscript.isNotBlank()) {
                        Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Latest spoken response", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(liveState.latestTranscript.take(900), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Text(
                        "Live AI Guide needs the server-side BOSON_API_KEY Firebase secret and a valid signed-in/App Check session. If it is not configured, use Talk Now or the foreground “Hey Adventurer” mode.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "For safety, the live session will stop when a GPS-confirmed motorcycle/car session reaches the movement threshold or the app leaves the foreground. It does not send SOS, provide turn-by-turn navigation, or verify trail/road/water/weather conditions.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Chat and quick commands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (liveState.isConnected) "Live AI Guide is connected. Ask naturally; sent messages are processed in order."
                        else "Offline command mode is available now. Type a command below; open-ended AI chat needs Firebase, App Check and the BOSON_API_KEY server secret.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (chatLines.isEmpty()) {
                        Text("Try: Open Explore · Read my trip status · Open Story Studio · Switch to trekking", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    } else {
                        chatLines.takeLast(8).forEach { line ->
                            Surface(
                                color = if (line.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(if (line.isUser) "You" else "Adventurer", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(line.text, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it.take(500) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask or command…") },
                            maxLines = 3,
                        )
                        Button(onClick = { sendTypedMessage() }, enabled = chatInput.isNotBlank()) {
                            Icon(Icons.Filled.Send, contentDescription = "Send message")
                        }
                    }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Open Explore", "Read my trip status", "Open Story Studio", "Switch to trekking").forEach { command ->
                            FilterChip(selected = false, onClick = { chatInput = command; sendTypedMessage() }, label = { Text(command) })
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = { showPersonalization = !showPersonalization }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.TipsAndUpdates, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (showPersonalization) "Hide optional personalisation" else "Personalise your guide (optional)")
            }
        }

        if (showPersonalization) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Remember my adventure interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Saved on this device and supplied to Live Guide as personalization context. It never treats preferences as facts about a place.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text("Topics", fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdventureGuidePreferenceOptions.interestLabels.forEach { (key, label) ->
                            FilterChip(
                                selected = key in guidePreferences.interests,
                                onClick = {
                                    val next = if (key in guidePreferences.interests) {
                                        guidePreferences.interests - key
                                    } else guidePreferences.interests + key
                                    if (next.isNotEmpty()) saveGuidePreferences(guidePreferences.copy(interests = next))
                                },
                                label = { Text(label, maxLines = 1) },
                            )
                        }
                    }
                    Text("Story style", fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdventureGuidePreferenceOptions.storyStyleLabels.forEach { (key, label) ->
                            FilterChip(
                                selected = guidePreferences.storyStyle == key,
                                onClick = { saveGuidePreferences(guidePreferences.copy(storyStyle = key)) },
                                label = { Text(label, maxLines = 1) },
                            )
                        }
                    }
                    Text("Suggestion level", fontWeight = FontWeight.SemiBold)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdventureGuidePreferenceOptions.proactivityLabels.forEach { (key, label) ->
                            FilterChip(
                                selected = guidePreferences.proactivity == key,
                                onClick = { saveGuidePreferences(guidePreferences.copy(proactivity = key)) },
                                label = { Text(label, maxLines = 1) },
                            )
                        }
                    }
                    Text("Live narration tone", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Higgs Realtime currently supplies its default voice. These options guide delivery style; they are not named voice IDs or voice cloning.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdventureGuidePreferenceOptions.narrationStyleLabels.forEach { (key, label) ->
                            FilterChip(
                                selected = guidePreferences.voicePreset == key,
                                onClick = { saveGuidePreferences(guidePreferences.copy(voicePreset = key)) },
                                label = { Text(label, maxLines = 1) },
                            )
                        }
                    }
                    Text(
                        "Your current profile: ${preferencesStore.spokenSummary(guidePreferences)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How should your guide sound?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.CALM, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.CALM) }, label = { Text("Calm") })
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.BALANCED, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.BALANCED) }, label = { Text("Balanced") })
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.ENERGETIC, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.ENERGETIC) }, label = { Text("Energetic") })
                    }
                    Text("Calm/Balanced/Energetic adjusts the local Android TTS fallback. The live narration tone above guides wording; Higgs Realtime currently uses its default voice. This does not select or clone a named voice.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(2.dp))
                    Text("Guide style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.guideMode == AdventureGuideMode.WANDER,
                            onClick = {
                                controller.setGuideMode(AdventureGuideMode.WANDER)
                                liveGuideSession.setGuideMode(AdventureGuideMode.WANDER)
                                controller.say("Wander mode selected. I'll keep answers short and wait for you to ask.")
                            },
                            label = { Text("Wander · ask me") },
                        )
                        FilterChip(
                            selected = state.guideMode == AdventureGuideMode.TOUR,
                            onClick = {
                                controller.setGuideMode(AdventureGuideMode.TOUR)
                                liveGuideSession.setGuideMode(AdventureGuideMode.TOUR)
                                controller.say("Tour mode selected. Ask for an activity briefing whenever you want a longer guide summary.")
                            },
                            label = { Text("Tour · guided") },
                        )
                    }
                    Text("Tour/Wander changes how Live Guide is instructed to respond. At session start, Adventurer prefetches a small nearby-place list and an exact-title-matched Wikipedia introduction when available; the guide uses it only when relevant. Automatic movement-triggered narration and verified route/condition data are not enabled.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.TipsAndUpdates, contentDescription = null, tint = AmberGold)
                        Text("Try saying", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    VoiceCommandExample("“Hey Adventurer, open Explore”", "Open trip planning from any screen while hands-free is enabled.")
                    VoiceCommandExample("“Hey Adventurer, go back” / “next screen”", "Navigate without reaching for the bottom tabs.")
                    VoiceCommandExample("“Switch to trekking” / “switch to scuba”", "Change the selected activity to match the adventure.")
                    VoiceCommandExample("“Give me an activity briefing”", "Hear the selected activity's current in-app guidance and prototype limitations.")
                    VoiceCommandExample("“Read my trip status” / “pause recording”", "Hear local GPS-session details or pause/resume an active session.")
                    VoiceCommandExample("“Search for Niagara Falls”", "Open an external map search; results are not verified by Adventurer.")
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current adventure context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Activity: ${activityMode.displayName}", fontWeight = FontWeight.SemiBold)
                    Text(activityMode.homeHint, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    if (telemetry.isRecording) {
                        Text(
                            "Session: ${if (telemetry.isPaused) "paused" else "recording"} · %.1f km · %.0f km/h".format(telemetry.distanceKm, telemetry.currentSpeedKmH),
                            color = AdventureOrange,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Text("No activity session is currently recording.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "Safety boundary: this companion does not provide turn-by-turn navigation, live trail/road/water/weather conditions, dive guidance, emergency dispatch, or a substitute for official sources.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Privacy & platform behaviour", fontWeight = FontWeight.Bold)
                    Text("Microphone use starts only after you tap Talk now or explicitly enable hands-free. Wake-phrase listening runs only while Adventurer is foregrounded and pauses when the app backgrounds. Device-default recognition may process speech through the selected Android service; check that service's privacy settings.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("No raw audio or transcript is sent to Adventurer analytics. Live Guide sends audio to the configured Boson realtime provider and sends rounded-only coordinates through Adventurer's server-side nearby-place callable; the backend does not forward exact coordinates to the map provider. Wake-phrase listening remains foreground-only, and microphone capture stops when the app backgrounds.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private data class VoiceChatLine(
    val isUser: Boolean,
    val text: String,
    val id: String = java.util.UUID.randomUUID().toString(),
)

private fun VoiceCommandExample(command: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(command, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}
