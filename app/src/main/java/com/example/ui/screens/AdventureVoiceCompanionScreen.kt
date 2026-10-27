package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.voice.AdventureGuideMode
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
    onRequestVoice: (VoiceStartRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()
    val activityMode by viewModel.activityMode.collectAsState()
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
            Card(colors = CardDefaults.cardColors(containerColor = VoicePanel), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("How should your guide sound?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.CALM, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.CALM) }, label = { Text("Calm") })
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.BALANCED, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.BALANCED) }, label = { Text("Balanced") })
                        FilterChip(selected = state.voiceStyle == AdventureVoiceStyle.ENERGETIC, onClick = { controller.setVoiceStyle(AdventureVoiceStyle.ENERGETIC) }, label = { Text("Energetic") })
                    }
                    Text("The voice style adjusts the installed Android TTS rate and pitch; it does not clone a voice or call a generative voice model.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(2.dp))
                    Text("Guide style", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.guideMode == AdventureGuideMode.WANDER,
                            onClick = {
                                controller.setGuideMode(AdventureGuideMode.WANDER)
                                controller.say("Wander mode selected. I'll keep answers short and wait for you to ask.")
                            },
                            label = { Text("Wander · ask me") },
                        )
                        FilterChip(
                            selected = state.guideMode == AdventureGuideMode.TOUR,
                            onClick = {
                                controller.setGuideMode(AdventureGuideMode.TOUR)
                                controller.say("Tour mode selected. Ask for an activity briefing whenever you want a longer guide summary.")
                            },
                            label = { Text("Tour · guided") },
                        )
                    }
                    Text("Tour/Wander currently controls the voice interaction style. Automatic landmark narration and verified nearby-place research need a connected, source-backed place provider.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
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
                    VoiceCommandExample("“Play narration” / “read my story”", "Read the generated Story Studio narration in its selected language without navigating the player.")
                    VoiceCommandExample("Tap Talk now or Stop", "Start a voice turn or interrupt current speech. Spoken interruption during playback requires a future realtime barge-in engine.")
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
                    Text("No raw audio, transcript, or exact location is sent to Adventurer analytics by this feature. A true always-on assistant or real-time speech-to-speech backend is not enabled in this build.", color = VoiceMuted, style = MaterialTheme.typography.bodySmall)
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
