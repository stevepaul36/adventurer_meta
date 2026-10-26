package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HighAltitudeCyan
import com.example.viewmodel.AdventurerViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

data class GameEntity(
    val id: Long = Random.nextLong(),
    var x: Float, // -1.0f (left) to 1.0f (right)
    var z: Float, // 1.0f (far horizon) to 0.0f (screen bottom)
    val isBoost: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexMotoGameScreen(
    viewModel: AdventurerViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler { viewModel.navigateBack() }

    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    var playerX by remember { mutableStateOf(0f) } // -0.8f to +0.8f
    var speedKmH by remember { mutableStateOf(92.0) }
    var topSpeedKmH by remember { mutableStateOf(92.0) }
    var distanceKm by remember { mutableStateOf(0.0) }
    var score by remember { mutableStateOf(0) }
    var roadCurveOffset by remember { mutableStateOf(0f) }
    var roadScrollOffset by remember { mutableStateOf(0f) }

    val entities = remember { mutableStateListOf<GameEntity>() }

    // Game loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastSpawnTime = System.currentTimeMillis()
            while (isPlaying && !isGameOver) {
                delay(30) // ~33 FPS physics update

                // Update road scroll and distance
                val speedDelta = (speedKmH / 3600.0) * 0.03
                distanceKm = kotlin.math.round((distanceKm + speedDelta) * 100.0) / 100.0
                score += (speedKmH * 0.2).toInt()
                roadScrollOffset = (roadScrollOffset + 0.08f) % 1.0f
                roadCurveOffset = kotlin.math.sin(distanceKm * 4).toFloat() * 0.3f

                // Spawn obstacles / boosts
                val now = System.currentTimeMillis()
                if (now - lastSpawnTime > 900) {
                    lastSpawnTime = now
                    val isBoost = Random.nextFloat() > 0.65f
                    val spawnLane = (Random.nextFloat() * 1.6f) - 0.8f
                    entities.add(GameEntity(x = spawnLane, z = 1.0f, isBoost = isBoost))
                }

                // Move entities toward player
                val iterator = entities.iterator()
                while (iterator.hasNext()) {
                    val entity = iterator.next()
                    entity.z -= 0.035f * (speedKmH / 90.0).toFloat()

                    // Collision detection at z close to 0.15f
                    if (entity.z in 0.05f..0.22f) {
                        val distanceToPlayer = kotlin.math.abs(entity.x - playerX)
                        if (distanceToPlayer < 0.25f) {
                            if (entity.isBoost) {
                                score += 500
                                speedKmH = (speedKmH + 12.0).coerceAtMost(168.0)
                                topSpeedKmH = maxOf(topSpeedKmH, speedKmH)
                                iterator.remove()
                            } else {
                                // Hit rock! Game over
                                isGameOver = true
                                isPlaying = false
                            }
                        }
                    } else if (entity.z <= 0.0f) {
                        iterator.remove()
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Apex Moto 3D", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Mountain Pass Canyon Racer", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Reset
                        isPlaying = true
                        isGameOver = false
                        score = 0
                        distanceKm = 0.0
                        speedKmH = 95.0
                        playerX = 0f
                        entities.clear()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = AmberGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0F17))
            )
        },
        containerColor = Color(0xFF0B0F17),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top HUD Banner
            Surface(
                color = Color(0xFF131B2A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SCORE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text(String.format("%,d", score), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberGold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SPEED", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text("${speedKmH.toInt()} KM/H", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = HighAltitudeCyan)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("DISTANCE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                        Text("${distanceKm} KM", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AdventureOrange)
                    }
                }
            }

            // 3D Canvas Game Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            playerX = (playerX + (dragAmount.x / 400f)).coerceIn(-0.75f, 0.75f)
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val horizonY = h * 0.35f

                    // Sky & Himalayan mountain peaks background
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF0A0F1D), Color(0xFF1E293B)),
                            startY = 0f,
                            endY = horizonY
                        ),
                        size = Size(w, horizonY)
                    )

                    // Distant alpine ridge
                    val ridgePath = Path().apply {
                        moveTo(0f, horizonY)
                        lineTo(w * 0.2f, horizonY - 45f)
                        lineTo(w * 0.45f, horizonY - 15f)
                        lineTo(w * 0.7f, horizonY - 60f)
                        lineTo(w, horizonY - 20f)
                        lineTo(w, horizonY)
                        close()
                    }
                    drawPath(ridgePath, color = Color(0xFF334155))

                    // Ground / Canyon floor
                    drawRect(
                        color = Color(0xFF1A1F2C),
                        topLeft = Offset(0f, horizonY),
                        size = Size(w, h - horizonY)
                    )

                    // Perspective Asphalt Road
                    val roadTopWidth = w * 0.16f
                    val roadBottomWidth = w * 0.88f
                    val topCenterX = (w / 2) + (roadCurveOffset * w * 0.3f)
                    val bottomCenterX = w / 2

                    val roadPath = Path().apply {
                        moveTo(topCenterX - roadTopWidth / 2, horizonY)
                        lineTo(topCenterX + roadTopWidth / 2, horizonY)
                        lineTo(bottomCenterX + roadBottomWidth / 2, h)
                        lineTo(bottomCenterX - roadBottomWidth / 2, h)
                        close()
                    }
                    drawPath(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF263238), Color(0xFF10141D)),
                            startY = horizonY,
                            endY = h
                        ),
                        path = roadPath
                    )

                    // Road edge white barriers / stripes
                    drawLine(
                        color = Color(0xFFFF6D00),
                        start = Offset(topCenterX - roadTopWidth / 2, horizonY),
                        end = Offset(bottomCenterX - roadBottomWidth / 2, h),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color(0xFFFF6D00),
                        start = Offset(topCenterX + roadTopWidth / 2, horizonY),
                        end = Offset(bottomCenterX + roadBottomWidth / 2, h),
                        strokeWidth = 4f
                    )

                    // Center Dashed Line
                    for (i in 0..7) {
                        val segmentZ = ((i * 0.14f) + roadScrollOffset * 0.14f) % 1.0f
                        val segY = horizonY + (segmentZ * (h - horizonY))
                        val curRoadWidth = roadTopWidth + (segmentZ * (roadBottomWidth - roadTopWidth))
                        val segX = topCenterX + (segmentZ * (bottomCenterX - topCenterX))

                        drawLine(
                            color = AmberGold,
                            start = Offset(segX, segY),
                            end = Offset(segX, segY + (12f * (segmentZ + 0.3f))),
                            strokeWidth = 4f * (segmentZ + 0.4f)
                        )
                    }

                    // Render Obstacles and Turbo Boosts
                    entities.forEach { entity ->
                        val z = entity.z
                        if (z in 0.05f..1.0f) {
                            val entityY = horizonY + ((1.0f - z) * (h - horizonY))
                            val currentRoadW = roadTopWidth + ((1.0f - z) * (roadBottomWidth - roadTopWidth))
                            val curRoadCenterX = topCenterX + ((1.0f - z) * (bottomCenterX - topCenterX))
                            val entityX = curRoadCenterX + (entity.x * (currentRoadW * 0.4f))

                            val scale = (1.0f - z).coerceIn(0.2f, 1.0f)
                            val radius = 22.dp.toPx() * scale

                            if (entity.isBoost) {
                                // Turbo Canister
                                drawCircle(
                                    color = AmberGold,
                                    radius = radius,
                                    center = Offset(entityX, entityY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = radius * 0.4f,
                                    center = Offset(entityX, entityY)
                                )
                            } else {
                                // Mountain Canyon Rock Obstacle
                                drawCircle(
                                    color = Color(0xFF78716C),
                                    radius = radius,
                                    center = Offset(entityX, entityY)
                                )
                                drawCircle(
                                    color = DangerRed,
                                    radius = radius * 0.3f,
                                    center = Offset(entityX, entityY)
                                )
                            }
                        }
                    }

                    // Player Motorcycle at bottom
                    val playerScreenY = h - 70.dp.toPx()
                    val curRoadW = roadBottomWidth
                    val playerScreenX = bottomCenterX + (playerX * (curRoadW * 0.4f))

                    // Motorcycle Chassis & Rider
                    val leanTilt = (playerX * 18f)

                    // Bike shadow
                    drawOval(
                        color = Color(0x77000000),
                        topLeft = Offset(playerScreenX - 22.dp.toPx(), playerScreenY + 18.dp.toPx()),
                        size = Size(44.dp.toPx(), 14.dp.toPx())
                    )

                    // Rear wheel
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(playerScreenX - 6.dp.toPx(), playerScreenY + 4.dp.toPx()),
                        size = Size(12.dp.toPx(), 22.dp.toPx())
                    )

                    // Adventure Tank & Body
                    drawRoundRect(
                        color = AdventureOrange,
                        topLeft = Offset(playerScreenX - 12.dp.toPx(), playerScreenY - 20.dp.toPx()),
                        size = Size(24.dp.toPx(), 26.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )

                    // Handlebars
                    drawLine(
                        color = Color.White,
                        start = Offset(playerScreenX - 24.dp.toPx(), playerScreenY - 14.dp.toPx()),
                        end = Offset(playerScreenX + 24.dp.toPx(), playerScreenY - 14.dp.toPx()),
                        strokeWidth = 4.dp.toPx()
                    )

                    // Rider Helmet
                    drawCircle(
                        color = AmberGold,
                        radius = 11.dp.toPx(),
                        center = Offset(playerScreenX, playerScreenY - 32.dp.toPx())
                    )
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(playerScreenX - 7.dp.toPx(), playerScreenY - 35.dp.toPx()),
                        size = Size(14.dp.toPx(), 6.dp.toPx())
                    )
                }

                // If not playing, show Start overlay
                if (!isPlaying && !isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2A)),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.SportsMotorsports, contentDescription = null, tint = AdventureOrange, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("APEX MOTO 3D", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Weave through Himalayan canyon switchbacks, collect turbo boosts, and dodge boulders!", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = {
                                        isPlaying = true
                                        isGameOver = false
                                        score = 0
                                        distanceKm = 0.0
                                        speedKmH = 92.0
                                        playerX = 0f
                                        entities.clear()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("start_apex_moto_button")
                                ) {
                                    Text("LAUNCH CANYON RUN", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // On-screen Arcade Steer Buttons
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { playerX = (playerX - 0.22f).coerceAtLeast(-0.75f) },
                        modifier = Modifier.size(60.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Steer Left", tint = Color.White, modifier = Modifier.size(30.dp))
                    }

                    Text("DRAG OR TAP ARROWS TO LEAN", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))

                    FilledIconButton(
                        onClick = { playerX = (playerX + 0.22f).coerceAtMost(0.75f) },
                        modifier = Modifier.size(60.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Steer Right", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            }
        }
    }

    // Game Over Dialog
    if (isGameOver) {
        AlertDialog(
            onDismissRequest = { isGameOver = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = AdventureOrange)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CANYON RUN COMPLETE!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Final Score: ${String.format("%,d", score)} pts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberGold)
                    Text("Top Speed: ${topSpeedKmH.toInt()} km/h", style = MaterialTheme.typography.bodyMedium, color = HighAltitudeCyan)
                    Text("Distance Conquered: $distanceKm km", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Share this run with your riding club & community feed!", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordApexRun(score, topSpeedKmH, distanceKm)
                        isGameOver = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdventureOrange)
                ) {
                    Text("Post Run to Feed", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isGameOver = false }) {
                    Text("Play Again", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF131B2A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
