package com.shayshankrathore.irishvisadate.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF1565C0), Color(0xFFFF8F00),
    Color(0xFFAD1457), Color(0xFF6A1B9A), Color(0xFF00838F),
)

private data class Particle(
    val x: Float,       // 0..1 relative to canvas width
    val speed: Float,   // relative fall speed
    val size: Float,
    val color: Color,
    val phase: Float,   // horizontal sway offset
)

@Composable
fun VisaGrantedScreen(onBack: () -> Unit) {
    val particles = remember {
        List(80) {
            Particle(
                x = Random.nextFloat(),
                speed = 0.15f + Random.nextFloat() * 0.35f,
                size = 6f + Random.nextFloat() * 10f,
                color = CONFETTI_COLORS[Random.nextInt(CONFETTI_COLORS.size)],
                phase = Random.nextFloat() * 2 * Math.PI.toFloat(),
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fall",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                val y = ((progress * p.speed * 3f + p.phase / (2 * Math.PI.toFloat())) % 1f) * size.height
                val x = p.x * size.width + sin(progress * 6f + p.phase) * 40f
                drawCircle(color = p.color, radius = p.size, center = Offset(x, y))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "🇮🇪",
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Congratulations!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Safe travels to Ireland",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFA5D6A7),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Tip: Check your visa sticker carefully before travelling — verify the name, passport number, validity dates, and number of entries are all correct.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8E6C9),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            ) {
                Text("Back", color = Color.White)
            }
        }
    }
}
