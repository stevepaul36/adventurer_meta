package com.example

import com.example.data.repository.AdventurerRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventurerFollowStateTest {
    @Test
    fun followAndUnfollowUpdatesRepositoryState() {
        val repository = AdventurerRepository()

        assertTrue("Seed users should have followed stories", repository.followedAdventurerIds.value.contains("u2"))
        assertTrue(repository.followedAdventurerIds.value.contains("u3"))

        repository.toggleFollowAdventurer("u4")
        assertTrue(repository.followedAdventurerIds.value.contains("u4"))

        repository.toggleFollowAdventurer("u4")
        assertFalse(repository.followedAdventurerIds.value.contains("u4"))
        assertEquals(setOf("u2", "u3"), repository.followedAdventurerIds.value)
    }

    @Test
    fun currentUserCannotFollowThemself() {
        val repository = AdventurerRepository()
        val before = repository.followedAdventurerIds.value

        repository.toggleFollowAdventurer(repository.currentUser.id)

        assertEquals(before, repository.followedAdventurerIds.value)
    }

    @Test
    fun blankProfileIdsAreIgnored() {
        val repository = AdventurerRepository()
        val before = repository.followedAdventurerIds.value

        repository.toggleFollowAdventurer(" ")

        assertEquals(before, repository.followedAdventurerIds.value)
    }
}
