package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * High-energy adventure vertical video short data item.
 */
data class AdventureShort(
    val id: String,
    val title: String,
    val caption: String,
    val adventurerName: String,
    val adventurerAvatar: String,
    val gearAndContext: String,
    val location: String,
    val altitude: String,
    val durationText: String,
    val viewerCount: String,
    val isLive: Boolean = false,
    val thumbnailUrl: String,
    val likes: Int,
    val commentsCount: Int = 24,
    val speedKmH: Int = 84,
    val leanAngle: Int = 36,
    val tags: List<String> = emptyList(),
    val activityLabel: String = "Motorcycling"
)

val defaultAdventureShorts = listOf(
    AdventureShort(
        id = "short_1",
        title = "Hairpin Switchbacks at 18,000 ft",
        caption = "Carving the highest motorable road in sub-zero thin air. Throttle response was crisp! 🏍️💨",
        adventurerName = "Mia Torque",
        adventurerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "KTM 390 Adventure",
        location = "Khardung La Pass",
        altitude = "17,982 ft",
        durationText = "0:28",
        viewerCount = "4.2k",
        isLive = true,
        thumbnailUrl = "https://images.unsplash.com/photo-1558981403-c5f9899a28bc?auto=format&fit=crop&w=800&q=80",
        likes = 1248,
        commentsCount = 89,
        speedKmH = 78,
        leanAngle = 41,
        tags = listOf("#HighPass", "#KTM", "#Himalayas")
    ),
    AdventureShort(
        id = "short_2",
        title = "Banff Singletrack Ridge Drop",
        caption = "Dropping into the loose shale descent along the continental divide! Brakes smoking 🔥",
        adventurerName = "Clutch Cargo",
        adventurerAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "BMW R1250 GS",
        location = "Banff Alpine Ridge",
        altitude = "3,400 m",
        durationText = "0:42",
        viewerCount = "2.9k",
        thumbnailUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
        likes = 892,
        commentsCount = 45,
        speedKmH = 92,
        leanAngle = 34,
        tags = listOf("#Rockies", "#GravelBlast", "#GS")
    ),
    AdventureShort(
        id = "short_3",
        title = "Zanskar Frozen River Crossing",
        caption = "Ice sheets cracking under knobby tyres. -8°C and pure adrenaline! ❄️",
        adventurerName = "Tenzin Trail",
        adventurerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "RE Himalayan 450",
        location = "Zanskar Chaddar",
        altitude = "12,800 ft",
        durationText = "0:35",
        viewerCount = "5.1k",
        isLive = true,
        thumbnailUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=800&q=80",
        likes = 2140,
        commentsCount = 132,
        speedKmH = 46,
        leanAngle = 22,
        tags = listOf("#IceCrossing", "#Himalayan450", "#Zanskar")
    ),
    AdventureShort(
        id = "short_4",
        title = "Pacific Coast Golden Hour Blast",
        caption = "Chasing the Pacific sunset through Big Sur sea-spray fog. Unfiltered perfection.",
        adventurerName = "Alex Rider",
        adventurerAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Ducati DesertX",
        location = "Big Sur Highway 1",
        altitude = "650 ft",
        durationText = "0:50",
        viewerCount = "1.8k",
        thumbnailUrl = "https://images.unsplash.com/photo-1568772585407-9361f9bf3a87?auto=format&fit=crop&w=800&q=80",
        likes = 756,
        commentsCount = 38,
        speedKmH = 105,
        leanAngle = 39,
        tags = listOf("#CaliforniaCoast", "#SunsetRide", "#DesertX")
    ),
    AdventureShort(
        id = "short_5",
        title = "Spiti Moonland Lunar Drift",
        caption = "Kick up dust in the otherworldly eroded canyons of Spiti Valley. Lunar playground!",
        adventurerName = "Zara Moto",
        adventurerAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Yamaha Tenere 700",
        location = "Dhankar Monoliths",
        altitude = "13,600 ft",
        durationText = "0:30",
        viewerCount = "3.4k",
        thumbnailUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
        likes = 1680,
        commentsCount = 74,
        speedKmH = 88,
        leanAngle = 36,
        tags = listOf("#Spiti", "#Tenere700", "#Drift")
    ),
    AdventureShort(
        id = "short_6",
        title = "Sunrise above the cloud line",
        caption = "A quiet alpine start, a long climb, and a view that made every step worth it.",
        adventurerName = "Leah Ridge",
        adventurerAvatar = "https://images.unsplash.com/photo-1531123897727-8f129e1688ce?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Alpine trekking kit",
        location = "Banff National Park",
        altitude = "2,650 m",
        durationText = "0:37",
        viewerCount = "3.2k",
        thumbnailUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80",
        likes = 967,
        commentsCount = 54,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#Trekking", "#Rockies", "#Summit"),
        activityLabel = "Trekking"
    ),
    AdventureShort(
        id = "short_7",
        title = "Paddling into Howe Sound",
        caption = "Glassy water, coastal peaks and a slow morning exploring the inlet by kayak.",
        adventurerName = "Noah Tide",
        adventurerAvatar = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Sea kayak and dry suit",
        location = "Howe Sound, British Columbia",
        altitude = "Sea level",
        durationText = "0:44",
        viewerCount = "2.6k",
        thumbnailUrl = "https://images.unsplash.com/photo-1500375592092-40eb2168fd21?auto=format&fit=crop&w=800&q=80",
        likes = 741,
        commentsCount = 31,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#Kayaking", "#Coast", "#ExploreBC"),
        activityLabel = "Kayaking"
    ),
    AdventureShort(
        id = "short_8",
        title = "Rain-on-tent night in the pines",
        caption = "A dry camp, warm food, a forest full of sound and a slow sunrise after the storm.",
        adventurerName = "Sage Wilds",
        adventurerAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Backcountry camp kit",
        location = "Algonquin Provincial Park",
        altitude = "Trail camp",
        durationText = "0:31",
        viewerCount = "1.9k",
        thumbnailUrl = "https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?auto=format&fit=crop&w=800&q=80",
        likes = 603,
        commentsCount = 28,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#Camping", "#WildPlaces", "#Ontario"),
        activityLabel = "Camping & wildlife"
    ),
    AdventureShort(
        id = "short_9",
        title = "Gravel climb before the city wakes",
        caption = "Loose gravel, a steady cadence and one last climb before the coffee stop.",
        adventurerName = "Arjun Pedal",
        adventurerAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Gravel bike and repair roll",
        location = "Nandi Hills, India",
        altitude = "1,475 m",
        durationText = "0:29",
        viewerCount = "2.1k",
        thumbnailUrl = "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=800&q=80",
        likes = 814,
        commentsCount = 37,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#Cycling", "#Bikepacking", "#Gravel"),
        activityLabel = "Cycling & bikepacking"
    ),
    AdventureShort(
        id = "short_10",
        title = "The long way along the coast",
        caption = "No fixed schedule today: ocean viewpoints, small-town diners and roads worth taking slowly.",
        adventurerName = "Maya Miles",
        adventurerAvatar = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Scenic road-trip kit",
        location = "Sea-to-Sky Highway, Canada",
        altitude = "Coastal route",
        durationText = "0:41",
        viewerCount = "4.0k",
        thumbnailUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80",
        likes = 1104,
        commentsCount = 63,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#RoadTrip", "#CoastalDrive", "#SlowTravel"),
        activityLabel = "Scenic road trips"
    ),
    AdventureShort(
        id = "short_11",
        title = "A bluewater world below the surface",
        caption = "A calm, carefully planned dive with a buddy check and an unforgettable reef encounter.",
        adventurerName = "Kai Deep",
        adventurerAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Mask, fins and dive computer",
        location = "Vancouver Island, Canada",
        altitude = "Underwater",
        durationText = "0:36",
        viewerCount = "3.8k",
        thumbnailUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80",
        likes = 1341,
        commentsCount = 82,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#Scuba", "#OceanLife", "#DiveBuddy"),
        activityLabel = "Scuba diving"
    ),
    AdventureShort(
        id = "short_12",
        title = "Fly-fishing at first light",
        caption = "Quiet water, patient casting and a catch-and-release morning on a favourite bend in the river.",
        adventurerName = "Robin Cast",
        adventurerAvatar = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=200&q=80",
        gearAndContext = "Fly rod and wader kit",
        location = "Ontario backcountry",
        altitude = "River level",
        durationText = "0:33",
        viewerCount = "1.5k",
        thumbnailUrl = "https://images.unsplash.com/photo-1498654200943-1088dd4438ae?auto=format&fit=crop&w=800&q=80",
        likes = 522,
        commentsCount = 19,
        speedKmH = 0,
        leanAngle = 0,
        tags = listOf("#Fishing", "#CatchAndRelease", "#Outdoors"),
        activityLabel = "Fishing & angling"
    )
)

/**
 * Top-of-feed horizontal shorts & video reels strip with active viewer telemetry and pulse.
 */
@Composable
fun ExpeditionShortsReel(
    shorts: List<AdventureShort> = defaultAdventureShorts,
    onShortClick: (AdventureShort) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_reel")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expedition_shorts_reel")
    ) {
        // Section Header with active viewer badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DangerRed.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(DangerRed.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE NOW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = DangerRed,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "ADVENTURER SHORTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = AdventureOrange.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = AdventureOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "17.4k people watching",
                        style = MaterialTheme.typography.labelSmall,
                        color = AdventureOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Horizontal scrolling vertical reel cards (9:16 aspect ratio feel)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            shorts.forEachIndexed { index, shortItem ->
                ShortCard(
                    short = shortItem,
                    onClick = { onShortClick(shortItem) },
                    modifier = Modifier.testTag("short_card_${shortItem.id}")
                )
            }
        }
    }
}

/**
 * Individual 9:16 vertical short video preview card.
 */
@Composable
fun ShortCard(
    short: AdventureShort,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(false) }
    var currentLikes by remember { mutableIntStateOf(short.likes) }

    Card(
        modifier = modifier
            .width(160.dp)
            .height(260.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (short.isLive) DangerRed.copy(alpha = 0.8f) else AdventureOrange.copy(alpha = 0.6f),
                    Color.Transparent
                )
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Thumbnail image
            AsyncImage(
                model = short.thumbnailUrl,
                contentDescription = short.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dynamic dark gradients for top and bottom readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.65f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top Badges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live or Duration Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (short.isLive) DangerRed else Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (short.isLive) {
                            Text(
                                "LIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                short.durationText,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Viewer Count Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.RemoveRedEye,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            short.viewerCount,
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Central Glowing Play Icon
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f)),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play short",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom content: location, moment title, Adventurer identity and reactions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Location & Altitude tag
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = HighAltitudeCyan.copy(alpha = 0.25f),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "📍 ${short.location}",
                        color = HighAltitudeCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // Short Video Title
                Text(
                    text = short.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Adventurer identity with quick reaction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        AsyncImage(
                            model = short.adventurerAvatar,
                            contentDescription = "Adventurer ${short.adventurerName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${short.adventurerName} · Adventurer",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    // Like icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            isLiked = !isLiked
                            if (isLiked) currentLikes++ else currentLikes--
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) DangerRed else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$currentLikes",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Shorts Viewer with simulated playback, double-tap like,
 * live telemetry HUD overlay, active comments, sound toggle, and emoji burst reactions.
 */
@Composable
fun ActiveShortsViewerModal(
    short: AdventureShort,
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(short.likes) }
    var showHeartBurst by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var quickCommentText by remember { mutableStateOf("") }
    val floatingReactions = remember { mutableStateListOf<String>() }

    // Simulated playback progress 0f -> 1f
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(short.id, isPlaying) {
        if (isPlaying) {
            while (progress < 1f) {
                delay(100)
                progress += 0.015f
            }
            // Auto advance or loop
            progress = 0f
            onNext()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("active_shorts_viewer_modal")
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isLiked = true
                            likeCount++
                            showHeartBurst = true
                            coroutineScope.launch {
                                delay(900)
                                showHeartBurst = false
                            }
                        },
                        onTap = {
                            isPlaying = !isPlaying
                        }
                    )
                }
        ) {
            // Full screen video background / thumbnail simulation
            AsyncImage(
                model = short.thumbnailUrl,
                contentDescription = short.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Vignette shading
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
            )

            // Double tap heart burst animation
            AnimatedVisibility(
                visible = showHeartBurst,
                enter = scaleIn(spring(dampingRatio = 0.4f)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(100.dp)
                )
            }

            // Top Progress Bar & Header controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter)
            ) {
                // Segmented Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AdventureOrange,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Header: Adventurer identity, activity, viewing context, mute and close controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = short.adventurerAvatar,
                            contentDescription = "Adventurer ${short.adventurerName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, AdventureOrange, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${short.adventurerName} · Adventurer",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DangerRed
                                ) {
                                    Text(
                                        text = if (short.isLive) "LIVE" else "ADVENTURER",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${short.activityLabel} · ${short.viewerCount} watching · ${short.altitude}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Sound toggle
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier.testTag("viewer_sound_toggle")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White
                            )
                        }

                        // Close Modal
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("viewer_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close viewer",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Floating Emoji reaction bubbles rising upward
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp, end = 80.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                floatingReactions.takeLast(6).forEachIndexed { i, emoji ->
                    Text(
                        text = emoji,
                        fontSize = (26 + i * 2).sp,
                        modifier = Modifier
                            .offset(y = (-40 * (floatingReactions.size - i)).dp)
                            .scale(0.85f + (i * 0.05f))
                    )
                }
            }

            // Right-side interactive action icons (Like, Comment, Share, Previous, Next)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            if (isLiked) likeCount++ else likeCount--
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("viewer_like_button")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like short",
                            tint = if (isLiked) DangerRed else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "$likeCount",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Comment Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showCommentsSheet = !showCommentsSheet },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("viewer_comment_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${short.commentsCount}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Share Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Watch ${short.title} on Adventurer! ${short.caption}")
                                type = "text/plain"
                            }
                            try {
                                context.startActivity(Intent.createChooser(sendIntent, "Share Adventure Short"))
                            } catch (_: ActivityNotFoundException) {}
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("viewer_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share short",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "Share",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Previous Short
                IconButton(
                    onClick = {
                        progress = 0f
                        onPrevious()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("viewer_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Previous Short",
                        tint = Color.White
                    )
                }

                // Next Short
                IconButton(
                    onClick = {
                        progress = 0f
                        onNext()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("viewer_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next Short",
                        tint = Color.White
                    )
                }
            }

            // Bottom Overlay: activity-aware context, moment title, and reactions
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 74.dp, bottom = 20.dp)
            ) {
                // Live Trail Telemetry HUD
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HighAltitudeCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (short.activityLabel.contains("Motorcycling", true) || short.activityLabel.contains("Cycling", true)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = null, tint = HighAltitudeCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${short.speedKmH.takeIf { it > 0 }?.let { "$it km/h" } ?: "Moving"}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (short.activityLabel.contains("Motorcycling", true)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ScreenRotation, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${short.leanAngle}° Lean", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Explore, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(short.activityLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Terrain, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(short.altitude, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Short title and activity-specific gear/context
                Text(
                    text = short.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${short.activityLabel} · ${short.location} · ${short.gearAndContext}",
                    color = AmberGold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                Text(
                    text = short.caption,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Fast One-Tap Reaction Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("🔥 Wild", "🧭 Explore", "🏔️ Summit", "🌊 Flow", "❤️ Inspiring").forEach { reaction ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable {
                                floatingReactions.add(reaction.take(2))
                            }
                        ) {
                            Text(
                                text = reaction,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Pause Indicator if paused
            if (!isPlaying) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Paused",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Interactive Expedition Photo Viewer modal with caption, altitude tag, zoom affordance,
 * and double-tap like.
 */
@Composable
fun ActivePhotoViewerDialog(
    photoUrl: String,
    caption: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isLiked by remember { mutableStateOf(false) }
    var showHeartBurst by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.94f))
                .testTag("active_photo_viewer_dialog")
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            isLiked = true
                            showHeartBurst = true
                            coroutineScope.launch {
                                delay(900)
                                showHeartBurst = false
                            }
                        }
                    )
                }
        ) {
            // High resolution photo
            AsyncImage(
                model = photoUrl,
                contentDescription = caption,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 60.dp)
            )

            // Top bar with close button and photo badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPEDITION CAPTURE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("photo_viewer_close_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Double-tap heart burst
            AnimatedVisibility(
                visible = showHeartBurst,
                enter = scaleIn(spring(dampingRatio = 0.4f)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(96.dp)
                )
            }

            // Bottom Caption Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = caption,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { isLiked = !isLiked },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) DangerRed else Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Double-tap photo to like • High-Res Trail View",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

