package org.example.project.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.example.project.sensor.SensorData
import org.example.project.sensor.SensorManager
import kotlin.math.*

// ─── Physics constants ────────────────────────────────────────────────────────
private const val BALL_RADIUS        = 80f
private const val SENSOR_SCALE       = 600f       // sensor units → px/s² acceleration
private const val DAMPING            = 0.72f      // bounce energy retention (0=no bounce, 1=perfect)
private const val FRICTION           = 0.985f     // rolling friction per frame
private const val AIR_DRAG           = 0.999f     // air resistance per frame
private const val SHAKE_THRESHOLD    = 18f        // m/s² raw sensor magnitude to detect shake
private const val SHAKE_IMPULSE      = 1400f      // px/s impulse applied on shake
private const val TICK_MS            = 16L        // ~60 fps
private const val TICK_S             = TICK_MS / 1000f

// ─── State ────────────────────────────────────────────────────────────────────
private data class BallState(
    val x: Float,
    val y: Float,
    val vx: Float = 0f,
    val vy: Float = 0f
)

// ─── Composable ───────────────────────────────────────────────────────────────
@Composable
fun PhysicsBall(sensorManager: SensorManager) {

    // --- Sensor state ---
    var sensorData by remember { mutableStateOf(SensorData()) }
    var prevSensor by remember { mutableStateOf(SensorData()) }
    var gravityVec by remember { mutableStateOf(SensorData()) } // low-pass filter for gravity

    // --- Ball state (mutable individually so recompose is cheap) ---
    var ballX  by remember { mutableStateOf(400f) }
    var ballY  by remember { mutableStateOf(800f) }
    var velX   by remember { mutableStateOf(0f) }
    var velY   by remember { mutableStateOf(0f) }

    // --- Drag/fling state ---
    var isDragging  by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val velocityTracker = remember { VelocityTracker() }

    // --- Canvas size (updated each draw) ---
    var canvasW by remember { mutableStateOf(1080f) }
    var canvasH by remember { mutableStateOf(2000f) }

    // Sensor listener
    DisposableEffect(Unit) {
        sensorManager.startListening { data -> sensorData = data }
        onDispose { sensorManager.stopListening() }
    }

    // Physics tick
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(TICK_MS)

            if (!isDragging) {
                // Low-pass filter to isolate gravity
                val alpha = 0.8f
                val gx = alpha * gravityVec.x + (1 - alpha) * sensorData.x
                val gy = alpha * gravityVec.y + (1 - alpha) * sensorData.y
                val gz = alpha * gravityVec.z + (1 - alpha) * sensorData.z
                gravityVec = SensorData(gx, gy, gz)

                // High-pass filter to isolate linear acceleration (shake)
                val linearX = sensorData.x - gravityVec.x
                val linearY = sensorData.y - gravityVec.y
                val linearZ = sensorData.z - gravityVec.z
                
                val shakeMag = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)

                // Only apply shake if the movement is sudden and strong
                if (shakeMag > SHAKE_THRESHOLD * 0.5f) { // Adjusted threshold because we removed gravity from it
                    val angle = atan2(linearY, linearX)
                    velX += cos(angle) * SHAKE_IMPULSE
                    velY += sin(angle) * SHAKE_IMPULSE
                }
                prevSensor = sensorData

                // Tilt accelerations from sensor (using raw gravity vector)
                // Note: On Android, event.values[1] (Y) is positive when phone is upright.
                // We want positive Y to pull the ball down (positive screen Y).
                val ax = sensorData.x * SENSOR_SCALE
                val ay = sensorData.y * SENSOR_SCALE

                // Apply sensor-driven acceleration (accelerometer IS gravity)
                velX = (velX + ax * TICK_S) * FRICTION * AIR_DRAG
                velY = (velY + ay * TICK_S) * AIR_DRAG

                var nx = ballX + velX * TICK_S
                var ny = ballY + velY * TICK_S

                val minX = BALL_RADIUS
                val maxX = canvasW - BALL_RADIUS
                val minY = BALL_RADIUS
                val maxY = canvasH - BALL_RADIUS

                // Wall bounces
                if (nx < minX) { nx = minX; velX = abs(velX) * DAMPING }
                if (nx > maxX) { nx = maxX; velX = -abs(velX) * DAMPING }
                if (ny < minY) { ny = minY; velY = abs(velY) * DAMPING }
                if (ny > maxY) { ny = maxY; velY = -abs(velY) * DAMPING }

                ballX = nx
                ballY = ny
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Only attach if the touch is on the ball
                        val dx = offset.x - ballX
                        val dy = offset.y - ballY
                        if (sqrt(dx * dx + dy * dy) <= BALL_RADIUS * 2f) {
                            isDragging = true
                            dragOffsetX = dx
                            dragOffsetY = dy
                            velocityTracker.resetTracking()
                        }
                    },
                    onDrag = { change, _ ->
                        if (isDragging) {
                            velocityTracker.addPointerInputChange(change)
                            ballX = change.position.x - dragOffsetX
                            ballY = change.position.y - dragOffsetY
                            // clamp to screen
                            ballX = ballX.coerceIn(BALL_RADIUS, canvasW - BALL_RADIUS)
                            ballY = ballY.coerceIn(BALL_RADIUS, canvasH - BALL_RADIUS)
                        }
                    },
                    onDragEnd = {
                        if (isDragging) {
                            isDragging = false
                            val v: Velocity = velocityTracker.calculateVelocity()
                            velX = v.x.coerceIn(-3000f, 3000f)
                            velY = v.y.coerceIn(-3000f, 3000f)
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        velX = 0f; velY = 0f
                    }
                )
            }
    ) {
        canvasW = size.width
        canvasH = size.height

        drawBall(ballX, ballY, isDragging)
    }
}

// ─── Drawing helper ───────────────────────────────────────────────────────────
private fun DrawScope.drawBall(x: Float, y: Float, isGrabbed: Boolean) {
    val r = BALL_RADIUS

    // Shadow
    drawCircle(
        color = Color(0x55000000),
        radius = r * 0.9f,
        center = Offset(x + r * 0.3f, y + r * 0.35f)
    )

    // Main ball gradient
    val gradient = Brush.radialGradient(
        colors = if (isGrabbed) listOf(
            Color(0xFFFFD54F),
            Color(0xFFFFA000),
            Color(0xFFE65100)
        ) else listOf(
            Color(0xFF80DEEA),
            Color(0xFF00BCD4),
            Color(0xFF006064)
        ),
        center = Offset(x - r * 0.3f, y - r * 0.3f),
        radius = r * 1.4f
    )
    drawCircle(brush = gradient, radius = r, center = Offset(x, y))

    // Specular highlight
    drawCircle(
        color = Color(0xAAFFFFFF),
        radius = r * 0.35f,
        center = Offset(x - r * 0.28f, y - r * 0.28f)
    )
}
