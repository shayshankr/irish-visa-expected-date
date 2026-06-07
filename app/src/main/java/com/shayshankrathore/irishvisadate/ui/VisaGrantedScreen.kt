package com.shayshankrathore.irishvisadate.ui

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF1565C0), Color(0xFFFF8F00),
    Color(0xFFAD1457), Color(0xFF6A1B9A), Color(0xFF00838F),
)

private data class Particle(
    val x: Float, val speed: Float, val size: Float, val color: Color, val phase: Float,
)

@Composable
fun VisaGrantedScreen(onBack: () -> Unit, onVisaValidity: (() -> Unit)? = null) {
    val context = LocalContext.current

    // Trigger in-app review after confetti starts
    LaunchedEffect(Unit) {
        delay(2000)
        try {
            val manager = ReviewManagerFactory.create(context)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    (context as? Activity)?.let { activity ->
                        manager.launchReviewFlow(activity, task.result)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    val particles = remember {
        List(80) {
            Particle(
                x = Random.nextFloat(),
                speed = 0.15f + Random.nextFloat() * 0.35f,
                size  = 6f + Random.nextFloat() * 10f,
                color = CONFETTI_COLORS[Random.nextInt(CONFETTI_COLORS.size)],
                phase = Random.nextFloat() * 2 * Math.PI.toFloat(),
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "fall",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1B5E20)),
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
            Text("🇮🇪", style = MaterialTheme.typography.displayLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                "Congratulations!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Safe travels to Ireland",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFA5D6A7), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Tip: Check your visa sticker carefully before travelling — verify the name, passport number, validity dates, and number of entries are all correct.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC8E6C9), textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            if (onVisaValidity != null) {
                Button(
                    onClick = onVisaValidity,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    modifier = Modifier.fillMaxWidth(0.75f),
                ) { Text("📋 Calculate visa validity", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            ) { Text("Back", color = Color.White) }
        }
    }
}
