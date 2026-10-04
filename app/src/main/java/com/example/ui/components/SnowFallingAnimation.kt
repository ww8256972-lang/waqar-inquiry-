package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

data class Snowflake(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    val driftPhase: Float,
    val driftFrequency: Float
)

@Composable
fun SnowFallingAnimation(
    modifier: Modifier = Modifier,
    snowCount: Int = 75
) {
    val snowflakes = remember {
        List(snowCount) {
            Snowflake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3.5f + 1.8f,
                speed = Random.nextFloat() * 0.0018f + 0.0009f,
                alpha = Random.nextFloat() * 0.55f + 0.35f,
                driftPhase = Random.nextFloat() * 6.28f,
                driftFrequency = Random.nextFloat() * 1.5f + 0.8f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "snow_transition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snow_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        snowflakes.forEach { flake ->
            // Advance vertical position
            flake.y = (flake.y + flake.speed) % 1f

            // Calculate horizontal wave drift
            val horizontalOffset = sin(time * flake.driftFrequency + flake.driftPhase) * 28f
            val currentX = (flake.x * width + horizontalOffset).mod(width)
            val currentY = flake.y * height

            // Draw glowing halo
            drawCircle(
                color = Color.White.copy(alpha = flake.alpha * 0.35f),
                radius = flake.radius * 1.8f,
                center = Offset(currentX, currentY)
            )

            // Draw crisp snowflake core
            drawCircle(
                color = Color.White.copy(alpha = flake.alpha),
                radius = flake.radius,
                center = Offset(currentX, currentY)
            )
        }
    }
}
