package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class FlowerPetal(
    var x: Float,
    var y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val color: Color
)

@Composable
fun FloatingFlowerPetals(
    modifier: Modifier = Modifier,
    petalCount: Int = 28
) {
    val petalColors = listOf(
        Color(0xFFFFB7B2), // Sakura Pink
        Color(0xFFFFDAC1), // Peach Blossom
        Color(0xFFE2F0CB), // Fresh Sprout Green
        Color(0xFFFF9AA2), // Soft Rose
        Color(0xFFFFE57F)  // Marigold Gold
    )

    val petals = remember {
        List(petalCount) {
            FlowerPetal(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 12f + 10f,
                speedX = Random.nextFloat() * 0.0008f + 0.0003f,
                speedY = Random.nextFloat() * 0.0012f + 0.0006f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = Random.nextFloat() * 2f - 1f,
                color = petalColors[it % petalColors.size]
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "petal_transition")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "petal_time"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        petals.forEach { petal ->
            // Advance positions with gentle horizontal drift
            petal.y = (petal.y + petal.speedY) % 1f
            petal.x = (petal.x + petal.speedX + sin(animTime * 0.05f + petal.y * 10f) * 0.0004f).mod(1f)
            petal.rotation = (petal.rotation + petal.rotationSpeed) % 360f

            val px = petal.x * width
            val py = petal.y * height

            rotate(degrees = petal.rotation, pivot = Offset(px, py)) {
                val path = Path().apply {
                    moveTo(px, py - petal.size)
                    cubicTo(
                        px + petal.size * 0.8f, py - petal.size * 0.5f,
                        px + petal.size * 0.8f, py + petal.size * 0.5f,
                        px, py + petal.size
                    )
                    cubicTo(
                        px - petal.size * 0.8f, py + petal.size * 0.5f,
                        px - petal.size * 0.8f, py - petal.size * 0.5f,
                        px, py - petal.size
                    )
                    close()
                }
                drawPath(path = path, color = petal.color.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
fun ScrollingFlowerBanner(
    modifier: Modifier = Modifier
) {
    val flowerIcons = listOf("🌸", "🌺", "🌼", "🪷", "🌻", "🌷", "🌹", "💐", "🌸", "🌺", "🌼", "🪷", "🌻", "🌷", "🌹", "💐")
    val infiniteTransition = rememberInfiniteTransition(label = "flower_scroll")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scroll_offset"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        color = Color(0xFFFCE4EC).copy(alpha = 0.65f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.offset(x = offsetX.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Duplicate elements to ensure smooth continuous scrolling
                (flowerIcons + flowerIcons).forEach { flower ->
                    Text(
                        text = flower,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
