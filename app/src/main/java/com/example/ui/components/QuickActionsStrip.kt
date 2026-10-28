package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Str
import com.example.ui.theme.*
import com.example.viewmodel.Screen
import com.example.viewmodel.SupportedLang

enum class ModuleCategory(val label: String) {
    ALL("All Tools"),
    OPERATIONS("🧭 Ops & Radar"),
    ROUTES("🗺️ Routes"),
    COMMUNITY("👥 Community")
}

data class QuickActionItem(
    val screen: Screen,
    val title: String,
    val subtitle: String,
    val category: ModuleCategory,
    val icon: ImageVector,
    val accentColor: Color
)

/**
 * Organizes the complete suite of adventure features into an intuitive, accessible hub.
 */
@Composable
fun AdventureModulesHub(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onOpenAllModules: () -> Unit,
    modifier: Modifier = Modifier,
    lang: SupportedLang = SupportedLang.ENGLISH
) {
    var selectedCategory by remember { mutableStateOf(ModuleCategory.ALL) }

    val allModules = remember {
        listOf(
            QuickActionItem(
                screen = Screen.JOURNEY,
                title = "Journey Command",
                subtitle = "Food cart & fuel range",
                category = ModuleCategory.OPERATIONS,
                icon = Icons.Default.Explore,
                accentColor = TrailOrange
            ),
            QuickActionItem(
                screen = Screen.RADAR,
                title = "Roadside Radar",
                subtitle = "Assistance & SOS beacons",
                category = ModuleCategory.OPERATIONS,
                icon = Icons.Default.Radar,
                accentColor = DangerRed
            ),
            QuickActionItem(
                screen = Screen.EXPEDITIONS,
                title = "India Expeditions",
                subtitle = "Curated high-pass trails",
                category = ModuleCategory.ROUTES,
                icon = Icons.Default.Terrain,
                accentColor = HighAltitudeCyan
            ),
            QuickActionItem(
                screen = Screen.VOICE_COMPANION,
                title = "Voice Companion",
                subtitle = "Realtime audio guide",
                category = ModuleCategory.OPERATIONS,
                icon = Icons.Default.Mic,
                accentColor = Color(0xFF818CF8)
            ),
            QuickActionItem(
                screen = Screen.ROUTE_BRIEF,
                title = "Route Brief",
                subtitle = "8-pt prep checklist",
                category = ModuleCategory.ROUTES,
                icon = Icons.Default.Checklist,
                accentColor = SafeMint
            ),
            QuickActionItem(
                screen = Screen.CLUBS,
                title = "Community Hub",
                subtitle = "Clubs, chat & challenges",
                category = ModuleCategory.COMMUNITY,
                icon = Icons.Default.Groups,
                accentColor = Color(0xFF38BDF8)
            ),
            QuickActionItem(
                screen = Screen.GAMIFICATION,
                title = "Rider Trophies",
                subtitle = "XP levels & 16 badges",
                category = ModuleCategory.COMMUNITY,
                icon = Icons.Default.EmojiEvents,
                accentColor = AmberGold
            ),
            QuickActionItem(
                screen = Screen.PRO_PAYWALL,
                title = "Adventurer Pro",
                subtitle = "Offline 3D topo maps",
                category = ModuleCategory.OPERATIONS,
                icon = Icons.Default.WorkspacePremium,
                accentColor = Color(0xFFFB7185)
            )
        )
    }

    val filteredModules = remember(selectedCategory) {
        if (selectedCategory == ModuleCategory.ALL) allModules
        else allModules.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header with All Modules Launcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ADVENTURE MODULES & TOOLS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TrailOrange,
                    fontSize = 11.sp
                )
                Text(
                    text = "Operational commands & exploration suite",
                    style = MaterialTheme.typography.bodySmall,
                    color = FieldMuted,
                    fontSize = 11.sp
                )
            }

            Surface(
                onClick = onOpenAllModules,
                shape = RoundedCornerShape(8.dp),
                color = SlatePanel,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(BorderNavy)
                ),
                modifier = Modifier.testTag("modules_view_all_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "14 Modules",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        Icons.Default.Widgets,
                        contentDescription = "View all modules",
                        tint = SafeMint,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModuleCategory.values().forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SlatePanelAlt,
                        selectedLabelColor = TrailOrange,
                        containerColor = SlatePanel,
                        labelColor = FieldMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) TrailOrange else BorderNavy
                    ),
                    modifier = Modifier.testTag("cat_filter_${cat.name.lowercase()}")
                )
            }
        }

        // Grid of Category Modules (2 Columns)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredModules.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Surface(
                            onClick = { onNavigate(item.screen) },
                            shape = RoundedCornerShape(14.dp),
                            color = SlatePanel,
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (currentScreen == item.screen) item.accentColor else BorderNavy)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 68.dp)
                                .testTag("quick_action_${item.screen.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = item.accentColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            tint = item.accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = FieldMuted,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Backwards-compatible wrapper for existing calls.
 */
@Composable
fun QuickActionsStrip(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    lang: SupportedLang = SupportedLang.ENGLISH
) {
    AdventureModulesHub(
        currentScreen = currentScreen,
        onNavigate = onNavigate,
        onOpenAllModules = { onNavigate(Screen.ROUTE_BRIEF) },
        modifier = modifier,
        lang = lang
    )
}
