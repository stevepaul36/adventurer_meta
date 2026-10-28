package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SafeMint
import com.example.ui.theme.TrailOrange
import com.example.viewmodel.Screen

/** The one source of truth for the 14 destinations shown in the all-features directory. */
enum class ModuleCategory(val label: String, val description: String) {
    ALL("All tools", "Every destination in the app"),
    PLAN_DISCOVER("Plan & discover", "Find a place, plan a route, prepare"),
    FIELD_SAFETY("Field & safety", "Run an activity and stay informed"),
    CREATE_SHARE("Create & share", "Turn an adventure into a story"),
    COMMUNITY_PROGRESS("Community & progress", "Meet people and track milestones"),
    ACCOUNT_UPGRADE("Account & upgrade", "Control your profile and plan"),
}

data class QuickActionItem(
    val screen: Screen,
    val title: String,
    val subtitle: String,
    val category: ModuleCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val isPrimaryDestination: Boolean = false,
)

object AdventureFeatureCatalog {
    /** Every unique navigation destination, grouped by the task a person is trying to do. */
    val all: List<QuickActionItem> = listOf(
        QuickActionItem(Screen.FEED, "Basecamp", "Recent adventures, feed and starting point", ModuleCategory.PLAN_DISCOVER, Icons.Default.Home, TrailOrange, true),
        QuickActionItem(Screen.EXPLORE_PLAN, "Field Atlas", "Discover places and build a trip plan", ModuleCategory.PLAN_DISCOVER, Icons.Default.Map, TrailOrange, true),
        QuickActionItem(Screen.JOURNEY, "Journey Command", "Fuel, food stops and trip-side utilities", ModuleCategory.PLAN_DISCOVER, Icons.Default.Explore, TrailOrange),
        QuickActionItem(Screen.EXPEDITIONS, "India Expeditions", "Browse curated expedition ideas", ModuleCategory.PLAN_DISCOVER, Icons.Default.Terrain, HighAltitudeCyan),
        QuickActionItem(Screen.ROUTE_BRIEF, "Route Brief & Checklist", "Review route context and prepare", ModuleCategory.PLAN_DISCOVER, Icons.Default.Checklist, SafeMint),

        QuickActionItem(Screen.LIVE_TRACKER, "Go / Field Mode", "Start, pause and review an activity", ModuleCategory.FIELD_SAFETY, Icons.Default.Speed, SafeMint, true),
        QuickActionItem(Screen.VOICE_COMPANION, "Voice Companion", "Talk or type to navigate and get help", ModuleCategory.FIELD_SAFETY, Icons.Default.Mic, Color(0xFF818CF8)),
        QuickActionItem(Screen.RADAR, "Safety & Roadside Help", "Open available roadside and SOS options", ModuleCategory.FIELD_SAFETY, Icons.Default.Radar, DangerRed),

        QuickActionItem(Screen.STORY_STUDIO, "Story Studio", "Create captions, recaps and story drafts", ModuleCategory.CREATE_SHARE, Icons.Default.AutoAwesome, Color(0xFFC084FC)),
        QuickActionItem(Screen.FINAL_REVIEW, "Final Review", "Review privacy and safety before sharing", ModuleCategory.CREATE_SHARE, Icons.Default.VerifiedUser, SafeMint),

        QuickActionItem(Screen.CLUBS, "Community Hub", "Clubs, conversations and challenges", ModuleCategory.COMMUNITY_PROGRESS, Icons.Default.Groups, Color(0xFF38BDF8), true),
        QuickActionItem(Screen.GAMIFICATION, "Achievements & Milestones", "See progress, levels and badges", ModuleCategory.COMMUNITY_PROGRESS, Icons.Default.EmojiEvents, AmberGold),

        QuickActionItem(Screen.PROFILE, "Trail Settings & Profile", "Manage preferences, permissions and privacy", ModuleCategory.ACCOUNT_UPGRADE, Icons.Default.Settings, AmberGold, true),
        QuickActionItem(Screen.PRO_PAYWALL, "Adventurer Pro", "Review plan options and premium features", ModuleCategory.ACCOUNT_UPGRADE, Icons.Default.WorkspacePremium, Color(0xFFFB7185)),
    )

    /** Six intentionally curated secondary actions for Basecamp; the directory contains everything. */
    val basecampShortcuts: List<QuickActionItem> = listOf(
        Screen.JOURNEY,
        Screen.EXPEDITIONS,
        Screen.VOICE_COMPANION,
        Screen.ROUTE_BRIEF,
        Screen.STORY_STUDIO,
        Screen.RADAR,
    ).mapNotNull { destination -> all.firstOrNull { it.screen == destination } }

    val primaryDestinations: List<QuickActionItem>
        get() = all.filter { it.isPrimaryDestination }

    val categories: List<ModuleCategory>
        get() = ModuleCategory.values().filter { it != ModuleCategory.ALL }
}
