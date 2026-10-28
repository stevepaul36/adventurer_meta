package com.example

import com.example.ui.components.defaultAdventureShorts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpeditionShortsTest {

    @Test
    fun defaultShortsListContainsValidActiveViewerContent() {
        val shorts = defaultAdventureShorts
        assertTrue("Shorts list should not be empty", shorts.isNotEmpty())
        assertEquals(5, shorts.size)

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
        }
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
