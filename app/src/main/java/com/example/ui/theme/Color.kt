package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// Adventurer Palette - Vibrant Solar Adventure & High Pass Outdoor Colors
val AdventureOrange = Color(0xFFFF8B43) // Mapped to official Trail Orange #FF8B43
val AdventureOrangeLight = Color(0xFFFFB07A)
val AdventureOrangeDark = Color(0xFFD84315)

// Official Adventurer UI/UX Concept System Tokens
val FieldNavy = Color(0xFF0A1119)
val SlatePanel = Color(0xFF111D28)
val SlatePanelAlt = Color(0xFF162532)
val BorderNavy = Color(0xFF263844)
val TrailOrange = Color(0xFFFF8B43)
val SafeMint = Color(0xFFA8DFC4)
val FieldText = Color(0xFFF4F1E9)
val FieldMuted = Color(0xFF94A3B8)

val AmberGold = Color(0xFFF59E0B)
val AmberGoldLight = Color(0xFFFCD34D)
val AmberGoldDark = Color(0xFFB45309)

val HighAltitudeCyan = Color(0xFF0284C7)
val HighAltitudeCyanLight = Color(0xFF38BDF8)
val MountainStream = Color(0xFF0EA5E9)

// Bright Daylight Theme Colors (Clean, energetic, sunlit outdoor aesthetic)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightSurfaceBorder = Color(0xFFE2E8F0)
val LightSurfaceHover = Color(0xFFEDE9FE)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextMutedLight = Color(0xFF94A3B8)

// Tactical Alpine Dark Theme Colors
val DarkBackground = Color(0xFF0B0F17)
val DarkSurface = Color(0xFF131B2A)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkSurfaceBorder = Color(0xFF334155)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Status & Accent Tokens
val DangerRed = Color(0xFFEF4444)
val SuccessGreen = Color(0xFF10B981)
val WarningYellow = Color(0xFFF59E0B)
val ElectricPurple = Color(0xFF8B5CF6)

val CardGlow = Color(0x33FF5722)

/** Returns the semantic surface color based on MaterialTheme */
@Composable
fun adventureSurfaceColor(): Color = MaterialTheme.colorScheme.surface

/** Returns the semantic background color based on MaterialTheme */
@Composable
fun adventureBackgroundColor(): Color = MaterialTheme.colorScheme.background

/** Returns the semantic border color based on MaterialTheme */
@Composable
fun adventureBorderColor(): Color = MaterialTheme.colorScheme.outline

/** Returns the primary text color based on MaterialTheme */
@Composable
fun adventureTextPrimary(): Color = MaterialTheme.colorScheme.onSurface

/** Returns the secondary text color based on MaterialTheme */
@Composable
fun adventureTextSecondary(): Color = MaterialTheme.colorScheme.onSurfaceVariant
