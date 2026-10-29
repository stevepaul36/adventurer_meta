package com.example

import com.example.data.model.FeedAudiencePolicy
import com.example.data.model.FeedItem
import com.example.data.model.Post
import com.example.data.model.RideShare
import com.example.data.model.RideStats
import com.example.data.model.RideStory
import com.example.data.model.StoryWaypoint
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedAudiencePolicyTest {
    private val mia = User("u2", "Mia Torque", "mia.jpg")
    private val clutch = User("u3", "Clutch Cargo", "clutch.jpg")
    private val alex = User("u1", "Alex Rider", "alex.jpg")

    private val feed = listOf(
        FeedItem.PostItem(Post(id = "p1", user = mia, timestamp = "Now", caption = "Pass opened")),
        FeedItem.RideShareItem(
            RideShare(
                id = "rs1", user = clutch, timestamp = "1h", title = "Gravel loop",
                mapImageUrl = "map.jpg", rideStats = RideStats(20.0, "1h", 250)
            )
        ),
        FeedItem.StoryItem(
            RideStory(
                id = "s1", user = alex, title = "Weekend", description = "A short walk",
                coverImageUrl = "cover.jpg", date = "Today",
                waypoints = listOf(StoryWaypoint("Lookout", 100, "View")),
                totalDistanceKm = 4.0, highestAltitudeM = 100
            )
        )
    )

    @Test
    fun followingViewIncludesAllSupportedFeedKindsFromFollowedAuthors() {
        val result = FeedAudiencePolicy.followingItems(feed, setOf("u2", "u3"))
        assertEquals(listOf("p1", "rs1"), result.map {
            when (it) {
                is FeedItem.PostItem -> it.post.id
                is FeedItem.RideShareItem -> it.rideShare.id
                is FeedItem.StoryItem -> it.story.id
            }
        })
    }

    @Test
    fun emptyFollowingSetReturnsNoItems() {
        assertTrue(FeedAudiencePolicy.followingItems(feed, emptySet()).isEmpty())
    }

    @Test
    fun originalFeedOrderIsPreserved() {
        val result = FeedAudiencePolicy.followingItems(feed, setOf("u3", "u2", "u1"))
        assertEquals(feed, result)
    }
}
