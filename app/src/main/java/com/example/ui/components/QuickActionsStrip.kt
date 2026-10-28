package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderNavy
import com.example.ui.theme.FieldMuted
import com.example.ui.theme.SlatePanel
import com.example.ui.theme.SlatePanelAlt
import com.example.ui.theme.TrailOrange
import com.example.viewmodel.Screen
import com.example.viewmodel.SupportedLang

/**
 * A small, curated Basecamp launchpad. The full directory lives in AdventureFeatureOrganizerSheet;
 * the feed stays focused on six common secondary actions instead of repeating bottom navigation.
 */
@Composable
fun AdventureModulesHub(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    onOpenAllModules: () -> Unit,
    modifier: Modifier = Modifier,
    lang: SupportedLang = SupportedLang.ENGLISH
) {
    val shortcuts = AdventureFeatureCatalog.basecampShortcuts

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MORE ADVENTURE TOOLS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TrailOrange,
                    fontSize = 11.sp
                )
                Text(
                    text = "Six shortcuts · one complete directory",
                    style = MaterialTheme.typography.bodySmall,
                    color = FieldMuted,
                    fontSize = 11.sp
                )
            }
            Surface(
                onClick = onOpenAllModules,
                shape = RoundedCornerShape(10.dp),
                color = SlatePanelAlt,
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(TrailOrange)),
                modifier = Modifier.testTag("modules_view_all_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "All 14 tools",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Browse all 14 features",
                        tint = TrailOrange,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            shortcuts.chunked(2).forEach { rowItems ->
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
                                .heightIn(min = 72.dp)
                                .testTag("quick_action_${item.screen.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = item.accentColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = null,
                                            tint = item.accentColor,
                                            modifier = Modifier.size(19.dp)
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
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** Backwards-compatible wrapper for existing calls; Basecamp passes the full-directory action. */
@Composable
fun QuickActionsStrip(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier,
    lang: SupportedLang = SupportedLang.ENGLISH,
    onOpenAllModules: () -> Unit = {}
) {
    AdventureModulesHub(
        currentScreen = currentScreen,
        onNavigate = onNavigate,
        onOpenAllModules = onOpenAllModules,
        modifier = modifier,
        lang = lang
    )
}
