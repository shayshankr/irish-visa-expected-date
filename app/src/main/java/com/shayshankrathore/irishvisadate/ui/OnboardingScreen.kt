package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.ui.theme.IrishOrange
import kotlinx.coroutines.launch

private data class OnboardingPage(val emoji: String, val title: String, val body: String)

private val PAGES = listOf(
    OnboardingPage(
        "☘️",
        "Irish Visa Tracker",
        "Calculate your expected Irish visa decision window. Just enter your submission date and we handle weekends, public holidays, and VAC transit automatically.",
    ),
    OnboardingPage(
        "🌍",
        "Pick Your Embassy",
        "We support 13 Irish embassies worldwide. Select the one that processes applications for your country — each has different transit times and holiday calendars.",
    ),
    OnboardingPage(
        "📅",
        "Your Decision Window",
        "See the earliest and latest dates when you can expect a decision. The progress bar updates daily so you always know where you stand.",
    ),
    OnboardingPage(
        "🔔",
        "Notifications & More",
        "Get reminded when your window opens, at the midpoint, and if overdue. Save multiple applications, export backups, view embassy contacts, and check your document checklist.",
    ),
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState { PAGES.size }
    val scope      = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF063320), Color(0xFF0F6B40)))),
    ) {
        // Skip button
        TextButton(
            onClick  = onComplete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
        ) {
            Text("Skip", color = Color.White.copy(alpha = 0.55f))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Page content
            HorizontalPager(
                state    = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                val p = PAGES[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(p.emoji, fontSize = 72.sp)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text       = p.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 26.sp,
                        color      = Color.White,
                        textAlign  = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text      = p.body,
                        style     = MaterialTheme.typography.bodyLarge,
                        color     = Color.White.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                    )
                }
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(PAGES.size) { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == i) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == i) IrishOrange
                                else Color.White.copy(alpha = 0.35f),
                            )
                    )
                }
            }

            // Prev / Next / Get Started buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 44.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }) {
                        Text("← Back", color = Color.White.copy(alpha = 0.65f))
                    }
                } else {
                    Spacer(Modifier.width(64.dp))
                }

                if (pagerState.currentPage < PAGES.size - 1) {
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IrishOrange),
                    ) {
                        Text("Next →", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onComplete,
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IrishOrange),
                    ) {
                        Text("Get Started", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
