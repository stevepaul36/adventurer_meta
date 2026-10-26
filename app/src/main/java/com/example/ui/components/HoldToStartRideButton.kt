package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AdventureOrange
import com.example.ui.theme.AmberGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HoldToStartRideButton(
    modifier: Modifier = Modifier,
    onRideStarted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = holdProgress,
        animationSpec = tween(durationMillis = 100),
        label = "hold_progress"
    )

    fun vibrateShort() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(40)
            }
        }
    }

    fun vibrateSuccess() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 140), -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(180)
            }
        }
    }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            val startTime = System.currentTimeMillis()
            val totalDuration = 1400L // 1.4 seconds to confirm ride launch
            while (isHolding) {
                val elapsed = System.currentTimeMillis() - startTime
                val current = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                holdProgress = current
                if (current >= 1f) {
                    vibrateSuccess()
                    onRideStarted()
                    isHolding = false
                    holdProgress = 0f
                    break
                }
                delay(30)
            }
        } else {
            holdProgress = 0f
        }
    }

    Box(
        modifier = modifier
            .size(96.dp)
            .testTag("hold_to_start_ride_button")
            // Screen-reader users can't time a press-and-hold, so expose a long-press action instead.
            .clearAndSetSemantics {
                role = Role.Button
                contentDescription = "Start ride"
                stateDescription = if (isHolding) "Starting" else "Press and hold to start"
                onLongClick(label = "Start ride") { vibrateSuccess(); onRideStarted(); true }
            },
        contentAlignment = Alignment.Center
    ) {
        // Outer progress ring canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            // Background track
            drawCircle(
                color = Color(0x33FF6D00),
                style = Stroke(width = strokeWidth)
            )
            // Animated progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(AdventureOrange, AmberGold, AdventureOrange)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Inner button surface with touch detector
        Surface(
            shape = CircleShape,
            color = if (isHolding) AdventureOrange else Color(0xFF1E293B),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(76.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHolding = true
                            vibrateShort()
                            tryAwaitRelease()
                            isHolding = false
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = if (isHolding) Color.Black else AmberGold,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = if (isHolding) "HOLDING" else "HOLD",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isHolding) Color.Black else Color.White
                )
            }
        }
    }
}
