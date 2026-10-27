package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.Str
import com.example.ui.theme.AmberGold
import com.example.viewmodel.Screen
import com.example.viewmodel.SupportedLang

data class QuickActionItem(
    val screen: Screen,
    val label: Str,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * Shortcuts to the screens that aren't bottom-bar tabs (the tabs are already one tap away),
 * laid out as a 2×2 grid of large, easy-to-hit tiles.
 */
@Composable
fun QuickActionsStrip(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    lang: SupportedLang = SupportedLang.ENGLISH,
) {
    val items = listOf(
        QuickActionItem(Screen.EXPEDITIONS, Str.QUICK_EXPEDITIONS, Icons.Default.Terrain, Color(0xFF38BDF8)),
        QuickActionItem(Screen.STORY_STUDIO, Str.QUICK_STUDIO, Icons.Default.AutoAwesome, Color(0xFFC084FC)),
        QuickActionItem(Screen.CLUBS, Str.QUICK_CLUBS, Icons.Default.Groups, Color(0xFF38BDF8)),
        QuickActionItem(Screen.GAMIFICATION, Str.QUICK_TROPHIES, Icons.Default.EmojiEvents, AmberGold),
        QuickActionItem(Screen.PRO_PAYWALL, Str.QUICK_PRO, Icons.Default.WorkspacePremium, Color(0xFFFB7185)),
    )

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    Surface(
                        onClick = { onNavigate(item.screen) },
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF131B2A),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(if (currentScreen == item.screen) item.accentColor else Color(0xFF1E293B))
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 64.dp)
                            .testTag("quick_action_${item.screen.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(10.dp), color = item.accentColor.copy(alpha = 0.18f), modifier = Modifier.size(36.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(item.icon, contentDescription = null, tint = item.accentColor, modifier = Modifier.size(20.dp))
                                }
                            }
                            Text(
                                text = item.label.get(lang),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
