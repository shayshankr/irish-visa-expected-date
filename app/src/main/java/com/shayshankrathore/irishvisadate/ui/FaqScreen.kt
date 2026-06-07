package com.shayshankrathore.irishvisadate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.ui.theme.*

private data class FaqItem(val question: String, val answer: String)

private val FAQ_ITEMS = listOf(
    FaqItem(
        "How is the decision window calculated?",
        "Your file reaches the Embassy after a VAC transit period (1–5 working days depending on your city). From that receipt date, the window spans the published min–max working days for your visa type. Weekends and public holidays for both Ireland and the host country are excluded.",
    ),
    FaqItem(
        "What happens if my passport expires before the decision?",
        "Your application may be invalidated. Irish immigration requires your passport to be valid for the duration of your intended stay. Ensure it is valid for at least 6 months beyond your planned travel date to Ireland.",
    ),
    FaqItem(
        "Can I travel while waiting for my visa?",
        "No. Your passport is held at the VAC and then couriered to the Irish Embassy. You cannot travel internationally without your passport during this period.",
    ),
    FaqItem(
        "What does the visa decisions list mean?",
        "The Embassy publishes a daily list of application reference numbers with decisions (approved or refused). It only includes applications decided that day — not all pending applications. Also check your email for direct notifications from the Embassy.",
    ),
    FaqItem(
        "What should I do if my application is overdue?",
        "Email the Visa Office quoting your reference number, full name, passport number, and VAC submission date. Use the subject line: \"Visa Application Status Enquiry\". Use the 📧 Email Visa Office button on the tracker screen for a pre-filled template.",
    ),
    FaqItem(
        "What is the VAC transit time?",
        "After you submit at the VAC, your physical documents are couriered to the Irish Embassy. This takes 1–5 working days depending on your city. The decision window starts only from the date the Embassy receives your file.",
    ),
    FaqItem(
        "Can I withdraw my application?",
        "Yes, contact the Visa Office directly to request a withdrawal. The visa application fee is non-refundable in all cases, including withdrawals.",
    ),
    FaqItem(
        "What does a Short Stay C visa mean?",
        "A type C visa covers stays of up to 90 days in Ireland for tourism, visiting family, or short business trips. A type D visa is for longer stays — study, join family, work permit, working holiday, etc.",
    ),
    FaqItem(
        "What is the biometric appointment date?",
        "Some VAC offices require an in-person biometric appointment (fingerprints and photo) before formally lodging your documents. Track this date separately from your official VAC submission date using the optional field in the tracker.",
    ),
    FaqItem(
        "How do I appeal a refusal?",
        "You can appeal within 2 months of the refusal date. Submit a detailed letter to the Visa Appeals Officer addressing each specific reason for refusal, along with new supporting evidence. The Refused screen shows your appeal deadline and links to the official appeals page.",
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(onBack: () -> Unit) {
    val expanded = remember { mutableStateListOf<Int>() }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("FAQ", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back", color = IrishGreen) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF063320),
                    titleContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(FAQ_ITEMS) { index, item ->
                FaqCard(
                    item = item,
                    isExpanded = index in expanded,
                    onToggle = { if (index in expanded) expanded.remove(index) else expanded.add(index) },
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun FaqCard(item: FaqItem, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.question,
                    fontWeight = FontWeight.SemiBold,
                    color = IrishGreenDark,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isExpanded) "▲" else "▼", color = IrishGreen, fontSize = 12.sp)
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = DividerGreen)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = item.answer,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}
