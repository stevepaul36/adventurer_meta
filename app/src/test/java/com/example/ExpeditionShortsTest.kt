package com.example

import com.example.ui.components.defaultAdventureShorts
import com.example.data.repository.AdventurerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpeditionShortsTest {

    @Test
    fun defaultShortsListContainsValidActiveViewerContent() {
        val shorts = defaultAdventureShorts
        assertTrue("Shorts list should not be empty", shorts.isNotEmpty())
        assertEquals(12, shorts.size)

        shorts.forEach { shortItem ->
            assertTrue("Short must have a non-blank ID", shortItem.id.isNotBlank())
            assertTrue("Short must have a title", shortItem.title.isNotBlank())
            assertTrue("Short must have a location", shortItem.location.isNotBlank())
            assertTrue("Short must have an altitude", shortItem.altitude.isNotBlank())
            assertTrue("Short must specify duration or LIVE", shortItem.durationText.isNotBlank())
            assertTrue("Viewer count should be present", shortItem.viewerCount.isNotBlank())
            assertTrue("Likes count must be non-negative", shortItem.likes >= 0)
            assertTrue("Lean angle must be realistic", shortItem.leanAngle in 0..60)
            assertTrue("Speed must be realistic", shortItem.speedKmH in 0..200)
            assertTrue("Every short must identify an adventure activity", shortItem.activityLabel.isNotBlank())
            assertTrue("Every short must identify its creator", !shortItem.adventurerId.isNullOrBlank())
        }
    }

    @Test
    fun everyShortCreatorCanBeFollowedFromTheRepository() {
        val shortCreatorIds = defaultAdventureShorts.mapNotNull { it.adventurerId }
        val knownAdventurerIds = AdventurerRepository().friendUsers.map { it.id }.toSet()

        assertEquals("Each short needs one unique creator ID", defaultAdventureShorts.size, shortCreatorIds.toSet().size)
        assertTrue("Every short creator should exist in discovery", shortCreatorIds.all { it in knownAdventurerIds })
    }

    @Test
    fun shortsRepresentMultipleAdventureActivities() {
        val activityLabels = defaultAdventureShorts.map { it.activityLabel }.toSet()
        assertTrue("Feed should show more than motorcycling", activityLabels.size >= 7)
        assertTrue(activityLabels.contains("Trekking"))
        assertTrue(activityLabels.contains("Kayaking"))
        assertTrue(activityLabels.contains("Scuba diving"))
        assertTrue(activityLabels.contains("Fishing & angling"))
    }

    @Test
    fun shortsListHasLiveBroadcastShorts() {
        val hasLive = defaultAdventureShorts.any { it.isLive }
        assertTrue("At least one short should be marked as LIVE", hasLive)
    }

    @Test
    fun shortsCyclingWrapsAroundCorrectly() {
        val list = defaultAdventureShorts
        val size = list.size
        val currentIndex = 0

        val nextIndex = (currentIndex + 1) % size
        assertEquals(1, nextIndex)

        val lastIndex = size - 1
        val wrapNext = (lastIndex + 1) % size
        assertEquals(0, wrapNext)

        val wrapPrev = (0 - 1 + size) % size
        assertEquals(lastIndex, wrapPrev)
    }
}
