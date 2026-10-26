package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.HighAltitudeCyan

@Composable
fun PostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF1E293B))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Author row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = post.user.avatarUrl,
                    contentDescription = post.user.name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${post.user.bikeModel} • ${post.timestamp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption
            Text(
                text = post.caption,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE2E8F0)
            )

            // Optional Ride Telemetry Badge
            post.rideStats?.let { stats ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatPill(icon = Icons.Default.Route, label = "${stats.distanceKm} km")
                        StatPill(icon = Icons.Default.Timer, label = stats.duration)
                        StatPill(icon = Icons.Default.Terrain, label = "+${stats.elevationGainM}m")
                        StatPill(icon = Icons.Default.ScreenRotation, label = "${stats.maxLeanAngleDeg}° Lean")
                    }
                }
            }

            // Image
            post.imageUrl?.let { img ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = img,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val heartTint by animateColorAsState(
                    targetValue = if (post.isLikedByMe) Color(0xFFEF4444) else Color(0xFF94A3B8),
                    animationSpec = spring(),
                    label = "heart_tint"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = heartTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${post.likes}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (post.isLikedByMe) Color(0xFFEF4444) else Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${post.comments.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = { /* Share */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RideShareCard(
    rideShare: RideShare,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rideshare_card_${rideShare.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = rideShare.user.avatarUrl,
                    contentDescription = rideShare.user.name,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = rideShare.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                    Text(
                        text = "Mapped by ${rideShare.user.name} • ${rideShare.timestamp}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map preview with GPX overlay badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = rideShare.mapImageUrl,
                    contentDescription = "Map route",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC0B0F17))
                            )
                        )
                )
                // Bottom stats strip on map
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📍 ${rideShare.rideStats.distanceKm} km",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⏱️ ${rideShare.rideStats.duration}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "⛰️ +${rideShare.rideStats.elevationGainM}m",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interaction
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (rideShare.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (rideShare.isLikedByMe) Color(0xFFEF4444) else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(text = "${rideShare.likes}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = onCommentClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(text = "${rideShare.comments.size}", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                }

                AssistChip(
                    onClick = { /* Export GPX */ },
                    label = { Text("Export GPX", color = HighAltitudeCyan) },
                    leadingIcon = {
                        Icon(Icons.Default.Download, contentDescription = null, tint = HighAltitudeCyan, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B))
                )
            }
        }
    }
}

@Composable
fun RideStoryCard(
    story: RideStory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ride_story_card_${story.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(AdventureOrange.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AdventureOrange.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "FEATURED EXPEDITION STORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = AdventureOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = story.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = story.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = story.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = story.coverImageUrl,
                    contentDescription = story.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xDD0B0F17))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🏔️ Highest Altitude: ${story.highestAltitudeM}m (${(story.highestAltitudeM * 3.28084).toInt()} ft)",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Waypoints: ${story.waypoints.joinToString(" ➔ ") { it.name }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
