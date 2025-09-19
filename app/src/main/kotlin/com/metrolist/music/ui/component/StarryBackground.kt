package com.metrolist.music.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float,
    val rotation: Float,
    val isPoint: Boolean // Some stars are points, others are crosses
)

data class ShootingStar(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val duration: Long,
    val delay: Long
)

@Composable
fun StarryBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 235,
    backgroundColor: Color = Color.Black,
    starColor: Color = Color.White
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "starry_transition")

    // Animation for twinkling effect
    val twinkleAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    val timeAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    // Shooting star animation
    val shootingStarTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shooting_stars"
    )

    val stars = remember {
        generateStars(starCount)
    }

    val shootingStars = remember {
        generateShootingStars()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val backgroundBrush = Brush.radialGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.8f),
                        backgroundColor,
                        Color.Black
                    ),
                    radius = size.maxDimension * 0.8f,
                    center = Offset(size.width * 0.5f, size.height * 0.3f)
                )
                onDrawBehind {
                    // Draw gradient background
                    drawRect(backgroundBrush)
                }
            }
    ) {
        // Draw stars
        stars.forEach { star ->
            val starX = star.x * size.width
            val starY = star.y * size.height
            val currentTime = twinkleAnimation * star.twinkleSpeed

            // Calculate twinkling alpha
            val twinkleAlpha = (sin(currentTime) * 0.3f + 0.7f).coerceIn(0.2f, 1f)
            val alpha = (star.baseAlpha * twinkleAlpha).coerceIn(0f, 1f)

            val currentStarColor = starColor.copy(alpha = alpha)
            val starSize = star.size * (1f + sin(currentTime * 1.2f) * 0.1f)

            if (star.isPoint) {
                // Draw circular star
                drawCircle(
                    color = currentStarColor,
                    radius = starSize / 2f,
                    center = Offset(starX, starY)
                )
            } else {
                // Draw cross-shaped star
                rotate(
                    degrees = star.rotation + timeAnimation * 10f,
                    pivot = Offset(starX, starY)
                ) {
                    drawStar(
                        color = currentStarColor,
                        center = Offset(starX, starY),
                        size = starSize
                    )
                }
            }
        }

        // Draw shooting stars
        drawShootingStars(
            shootingStars = shootingStars,
            animationProgress = shootingStarTime,
            canvasSize = size,
            starColor = starColor
        )
    }
}

private fun generateStars(count: Int): List<Star> {
    val random = Random(42) // Fixed seed for consistent star positions

    return (0 until count).map {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            size = random.nextFloat() * 7f + 1f, // Size between 1-8
            baseAlpha = random.nextFloat() * 0.6f + 0.4f, // Alpha between 0.4-1.0
            twinkleSpeed = random.nextFloat() * 0.8f + 0.4f, // Speed variation
            rotation = random.nextFloat() * 360f,
            isPoint = random.nextFloat() > 0.3f // 70% are points, 30% are crosses
        )
    }
}

private fun generateShootingStars(): List<ShootingStar> {
    val random = Random(123) // Different seed for variety
    return (0 until 4).map { index ->
        val startX = random.nextFloat() * 0.3f + 0.7f // Start from right side
        val startY = random.nextFloat() * 0.5f // Top half
        val endX = startX - (random.nextFloat() * 0.5f + 0.4f) // Move left
        val endY = startY + (random.nextFloat() * 0.4f + 0.3f) // Move down

        ShootingStar(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            duration = random.nextLong(1500, 3000),
            delay = index * 2000L + random.nextLong(0, 1000) // Stagger the shooting stars
        )
    }
}

private fun DrawScope.drawShootingStars(
    shootingStars: List<ShootingStar>,
    animationProgress: Float,
    canvasSize: Size,
    starColor: Color
) {
    val currentTime = (animationProgress * 10000).toLong()

    shootingStars.forEach { shootingStar ->
        val starStartTime = shootingStar.delay
        val starEndTime = starStartTime + shootingStar.duration

        if (currentTime >= starStartTime && currentTime <= starEndTime) {
            val progress =
                ((currentTime - starStartTime).toFloat() / shootingStar.duration.toFloat())
                    .coerceIn(0f, 1f)

            // Calculate current position
            val currentX =
                shootingStar.startX + (shootingStar.endX - shootingStar.startX) * progress
            val currentY =
                shootingStar.startY + (shootingStar.endY - shootingStar.startY) * progress

            val x = currentX * canvasSize.width
            val y = currentY * canvasSize.height

            // Calculate alpha with fade in/out effect
            val alpha = when {
                progress < 0.2f -> progress / 0.2f // Fade in
                progress > 0.8f -> (1f - progress) / 0.2f // Fade out
                else -> 1f // Full brightness
            }.coerceIn(0f, 1f)

            // Draw shooting star trail
            val trailLength = 80f
            val direction = Offset(
                shootingStar.endX - shootingStar.startX,
                shootingStar.endY - shootingStar.startY
            )
            val trailStart = Offset(
                x - direction.x * canvasSize.width * 0.15f,
                y - direction.y * canvasSize.height * 0.15f
            )

            // Draw trail with gradient
            val gradient = Brush.linearGradient(
                colors = listOf(
                    starColor.copy(alpha = 0f),
                    starColor.copy(alpha = alpha * 0.4f),
                    starColor.copy(alpha = alpha * 0.8f),
                    starColor.copy(alpha = alpha)
                ),
                start = trailStart,
                end = Offset(x, y)
            )

            drawLine(
                brush = gradient,
                start = trailStart,
                end = Offset(x, y),
                strokeWidth = 3f
            )

            // Draw bright head
            drawCircle(
                color = starColor.copy(alpha = alpha),
                radius = 4f,
                center = Offset(x, y)
            )

            // Draw bright glow around the head
            drawCircle(
                color = starColor.copy(alpha = alpha * 0.3f),
                radius = 8f,
                center = Offset(x, y)
            )
        }
    }
}

private fun DrawScope.drawStar(
    color: Color,
    center: Offset,
    size: Float
) {
    val halfSize = size / 2f
    val quarterSize = size / 4f

    // Draw vertical line
    drawLine(
        color = color,
        start = Offset(center.x, center.y - halfSize),
        end = Offset(center.x, center.y + halfSize),
        strokeWidth = 1f
    )

    // Draw horizontal line
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y),
        end = Offset(center.x + halfSize, center.y),
        strokeWidth = 1f
    )

    // Add small center dot for brightness
    drawCircle(
        color = color,
        radius = quarterSize,
        center = center
    )
}