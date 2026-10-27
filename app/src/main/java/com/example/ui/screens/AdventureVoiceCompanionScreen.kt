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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

private val VoicePanel = Color(0xFF131B2A)
private val VoiceMuted = Color(0xFF94A3B8)

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
                    "Use your voice to navigate Adventurer, control a session, and hear an activity-aware briefing.",
                    color = VoiceMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            color = if (state.handsFreeEnabled) AdventureOrange else Color(0xFF263246),
                            shape = CircleShape,
                        ) {
                            Icon(
                                if (state.isSpeaking) Icons.Filled.VolumeUp else Icons.Filled.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                when {
                                    state.isSpeaking -> "Adventurer is speaking"
                                    state.isListening -> "Listening"
                                    state.handsFreeEnabled -> "Hey Adventurer is armed"
                                    else -> "Voice is ready"
                                },
                                fontWeight = FontWeight.Bold,
                            )
                            Text(state.status, color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
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
                        color = VoiceMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        if (state.hasLocalTtsVoice) "Narration: a local/offline TTS voice is available for the selected language."
                        else "Narration: a local TTS voice is not confirmed. Check Android text-to-speech voice downloads for offline use.",
                        color = VoiceMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(22.dp)) {
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
                                color = if (liveState.isConnected) AdventureOrange else VoiceMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    Text(
                        "Speak naturally, interrupt the guide mid-sentence, ask follow-up questions, and ask what might be nearby. Adventurer can prefetch nearby map context at connection time; place identity and direction remain uncertain.",
                        color = VoiceMuted,
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
                            enabled = state.isReady || !liveState.isConnecting,
                        ) {
                            Icon(Icons.Filled.Headphones, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Start Live AI Guide")
                        }
                    }
                    Text(liveState.status, color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                    liveState.lastError?.takeIf { it.isNotBlank() }?.let {
                        Text(it, color = AmberGold, style = MaterialTheme.typography.bodySmall)
                    }
                    if (liveState.latestTranscript.isNotBlank()) {
                        Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Latest spoken response", fontWeight = FontWeight.Bold, color = Color.White)
                                Text(liveState.latestTranscript.take(900), color = VoiceMuted, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Text(
                        "Live AI Guide needs the server-side BOSON_API_KEY Firebase secret and a valid signed-in/App Check session. If it is not configured, use Talk Now or the foreground “Hey Adventurer” mode.",
                        color = VoiceMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        "For safety, the live session will stop when a GPS-confirmed motorcycle/car session reaches the movement threshold or the app leaves the foreground. It does not send SOS, provide turn-by-turn navigation, or verify trail/road/water/weather conditions.",
                        color = VoiceMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Remember my adventure interests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "Saved on this device and supplied to Live Guide as personalization context. It never treats preferences as facts about a place.",
                        color = VoiceMuted,
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
                        color = VoiceMuted,
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
                        color = VoiceMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How should your guide sound?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.CALM, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.CALM) }, label = { Text("Calm") })
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.BALANCED, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.BALANCED) }, label = { Text("Balanced") })
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.ENERGETIC, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.ENERGETIC) }, label = { Text("Energetic") })
                    }
                    Text("Calm/Balanced/Energetic adjusts the local Android TTS fallback. The live narration tone above guides wording; Higgs Realtime currently uses its default voice. This does not select or clone a named voice.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
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
                    Text("Tour/Wander changes how Live Guide is instructed to respond. At session start, Adventurer prefetches a small nearby-place list and an exact-title-matched Wikipedia introduction when available; the guide uses it only when relevant. Automatic movement-triggered narration and verified route/condition data are not enabled.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(20.dp)) {
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
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Current adventure context", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Activity: ${activityMode.displayName}", fontWeight = FontWeight.SemiBold)
                    Text(activityMode.homeHint, color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                    if (telemetry.isRecording) {
                        Text(
                            "Session: ${if (telemetry.isPaused) "paused" else "recording"} · %.1f km · %.0f km/h".format(telemetry.distanceKm, telemetry.currentSpeedKmH),
                            color = AdventureOrange,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Text("No activity session is currently recording.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "Safety boundary: this companion does not provide turn-by-turn navigation, live trail/road/water/weather conditions, dive guidance, emergency dispatch, or a substitute for official sources.",
                        color = VoiceMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Privacy & platform behaviour", fontWeight = FontWeight.Bold)
                    Text("Microphone use starts only after you tap Talk now or explicitly enable hands-free. Wake-phrase listening runs only while Adventurer is foregrounded and pauses when the app backgrounds. Device-default recognition may process speech through the selected Android service; check that service's privacy settings.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                    Text("No raw audio or transcript is sent to Adventurer analytics. Live Guide sends audio to the configured Boson realtime provider and sends rounded-only coordinates through Adventurer's server-side nearby-place callable; the backend does not forward exact coordinates to the map provider. Wake-phrase listening remains foreground-only, and microphone capture stops when the app backgrounds.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun VoiceCommandExample(command: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(command, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(description, color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
    }
}
