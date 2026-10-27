package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
            IconButton(onClick = { controller.stopSpeaking() }) {
                Icon(Icons.Filled.Stop, contentDescription = "Stop Adventurer speaking", tint = Color.White)
            }
        }
        Surface(
            onClick = { onRequestVoice(VoiceStartRequest.PUSH_TO_TALK) },
            color = if (state.handsFreeEnabled) AdventureOrange else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(100.dp),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Mic, contentDescription = null, tint = if (state.handsFreeEnabled) Color.Black else Color.White)
                Text(
                    if (state.handsFreeEnabled) "Hey Adventurer" else "Voice",
                    color = if (state.handsFreeEnabled) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
