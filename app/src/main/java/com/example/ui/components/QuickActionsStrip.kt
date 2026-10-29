package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
 * Compact Basecamp launchpad: horizontally scrollable, contextual tools only.
 * The searchable feature directory is the single home for the complete catalog.
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
                    text = "QUICK LINKS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TrailOrange,
                    fontSize = 11.sp
                )
                Text(
                    text = "Swipe for more · ${shortcuts.size} shortcuts",
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
                        text = "Browse ${AdventureFeatureCatalog.all.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Browse all features",
                        tint = TrailOrange,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("basecamp_quick_links"),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shortcuts.forEach { item ->
                Surface(
                    onClick = { onNavigate(item.screen) },
                    shape = RoundedCornerShape(14.dp),
                    color = SlatePanel,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(if (currentScreen == item.screen) item.accentColor else BorderNavy)
                    ),
                    modifier = Modifier
                        .width(190.dp)
                        .heightIn(min = 106.dp)
                        .testTag("quick_action_${item.screen.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(9.dp),
                                color = item.accentColor.copy(alpha = 0.16f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = item.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = FieldMuted,
                            fontSize = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                }
            }
        }
    }
}

/** Backwards-compatible wrapper for existing calls; Basecamp opens the full directory action. */
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
