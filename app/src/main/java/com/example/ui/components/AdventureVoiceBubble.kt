package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.voice.AdventureVoiceController
import com.example.data.voice.VoiceStartRequest
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.DangerRed

/** Always-available in-app voice entry point; the controller remains mounted as screens change. */
@Composable
fun AdventureVoiceBubble(
    controller: AdventureVoiceController,
    onRequestVoice: (VoiceStartRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by controller.state.collectAsState()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isSpeaking) {
            IconButton(
                onClick = { controller.stopSpeaking() },
                modifier = Modifier.size(40.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = DangerRed,
                    shadowElevation = 6.dp
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stop Adventurer speaking",
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
        val isHandsFree = state.handsFreeEnabled
        val isListening = state.isListening
        val bubbleColor = when {
            isListening -> DangerRed
            isHandsFree -> AdventureOrange
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        val contentColor = when {
            isListening || isHandsFree -> Color.White
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
        Surface(
            onClick = { onRequestVoice(VoiceStartRequest.PUSH_TO_TALK) },
            color = bubbleColor,
            shape = RoundedCornerShape(100.dp),
            shadowElevation = 6.dp,
            border = if (!isHandsFree && !isListening) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Voice assistant",
                    tint = if (!isHandsFree && !isListening) AdventureOrange else contentColor
                )
                Text(
                    text = when {
                        isListening -> "Listening…"
                        isHandsFree -> "Hey Adventurer"
                        else -> "Voice"
                    },
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
