package com.example

import com.example.ui.components.AdventureFeatureCatalog
import com.example.ui.components.ModuleCategory
import com.example.ui.components.filterAdventureFeatures
import com.example.viewmodel.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureFeatureCatalogTest {
    @Test
    fun directoryListsEachDestinationExactlyOnce() {
        val features = AdventureFeatureCatalog.all

        assertEquals(14, features.size)
        assertEquals(14, features.map { it.screen }.distinct().size)
    }

    @Test
    fun fivePrimaryTabsMatchTheNavigationArchitecture() {
        val primaryScreens = AdventureFeatureCatalog.primaryDestinations.map { it.screen }.toSet()

        assertEquals(5, primaryScreens.size)
        assertEquals(
            setOf(Screen.FEED, Screen.EXPLORE_PLAN, Screen.CLUBS, Screen.LIVE_TRACKER, Screen.PROFILE),
            primaryScreens
        )
    }

    @Test
    fun taskGroupsHaveTheAgreedFeatureCounts() {
        val features = AdventureFeatureCatalog.all

        assertEquals(5, features.count { it.category == ModuleCategory.PLAN_DISCOVER })
        assertEquals(3, features.count { it.category == ModuleCategory.FIELD_SAFETY })
        assertEquals(2, features.count { it.category == ModuleCategory.CREATE_SHARE })
        assertEquals(2, features.count { it.category == ModuleCategory.COMMUNITY_PROGRESS })
        assertEquals(2, features.count { it.category == ModuleCategory.ACCOUNT_UPGRADE })
    }


    @Test
    fun blankSearchReturnsTheCompleteDirectoryInCatalogOrder() {
        val results = filterAdventureFeatures("   ")

        assertEquals(AdventureFeatureCatalog.all.map { it.screen }, results.map { it.screen })
    }

    @Test
    fun searchIsCaseInsensitiveAndMatchesTitlesSubtitlesAndScreenNames() {
        assertEquals(
            listOf(Screen.VOICE_COMPANION),
            filterAdventureFeatures("  vOiCe  ").map { it.screen }
        )
        assertEquals(
            listOf(Screen.ROUTE_BRIEF),
            filterAdventureFeatures("checklist").map { it.screen }
        )
        assertEquals(
            listOf(Screen.PRO_PAYWALL),
            filterAdventureFeatures("premium").map { it.screen }
        )
    }

    @Test
    fun filtersCanCombineCategoryAndSearch() {
        assertEquals(
            listOf(Screen.RADAR),
            filterAdventureFeatures("safety", ModuleCategory.FIELD_SAFETY).map { it.screen }
        )
        assertTrue(
            filterAdventureFeatures("voice", ModuleCategory.COMMUNITY_PROGRESS).isEmpty()
        )
    }

    @Test
    fun allCategoryIsNotListedAsAFeatureGroup() {
        assertFalse(AdventureFeatureCatalog.categories.contains(ModuleCategory.ALL))
        assertEquals(5, AdventureFeatureCatalog.categories.size)
    }

    @Test
    fun basecampShortcutsAreSixSecondaryDestinations() {
        val shortcuts = AdventureFeatureCatalog.basecampShortcuts

        assertEquals(6, shortcuts.size)
        assertEquals(6, shortcuts.map { it.screen }.distinct().size)
        assertTrue(shortcuts.all { feature -> AdventureFeatureCatalog.all.any { it.screen == feature.screen } })
        assertFalse(shortcuts.any { it.isPrimaryDestination })
    }
}
