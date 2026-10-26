package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.Screen

data class QuickActionItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun QuickActionsStrip(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem(Screen.EXPEDITIONS, "Expeditions", Icons.Default.Terrain, AdventureOrange),
        QuickActionItem(Screen.RADAR, "Roadside Radar", Icons.Default.Emergency, Color(0xFFEF4444)),
        QuickActionItem(Screen.LIVE_TRACKER, "Telemetry HUD", Icons.Default.Speed, HighAltitudeCyan),
        QuickActionItem(Screen.APEX_MOTO, "Apex Moto 3D", Icons.Default.SportsMotorsports, AmberGold),
        QuickActionItem(Screen.STORY_STUDIO, "AI Story Studio", Icons.Default.AutoAwesome, Color(0xFFC084FC)),
        QuickActionItem(Screen.GAMIFICATION, "Trophy Hub", Icons.Default.EmojiEvents, AmberGold),
        QuickActionItem(Screen.CLUBS, "Riding Clubs", Icons.Default.Groups, Color(0xFF38BDF8))
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            Surface(
                onClick = { onNavigate(item.screen) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) item.accentColor.copy(alpha = 0.25f) else Color(0xFF1E293B),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isSelected) item.accentColor else Color(0xFF334155)
                    )
                ),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("quick_action_${item.screen.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) item.accentColor else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}
