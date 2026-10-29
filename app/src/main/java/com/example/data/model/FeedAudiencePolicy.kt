package com.example.data.model

/**
 * Applies the viewer's following relationship to a feed without mutating the original list.
 * Stories, posts and shared routes all use their author as the source of truth.
 */
object FeedAudiencePolicy {
    fun followingItems(
        items: List<FeedItem>,
        followedAdventurerIds: Set<String>
    ): List<FeedItem> {
        if (followedAdventurerIds.isEmpty()) return emptyList()
        return items.filter { item ->
            val authorId = when (item) {
                is FeedItem.PostItem -> item.post.user.id
                is FeedItem.RideShareItem -> item.rideShare.user.id
                is FeedItem.StoryItem -> item.story.user.id
            }
            authorId in followedAdventurerIds
        }
    }
}
