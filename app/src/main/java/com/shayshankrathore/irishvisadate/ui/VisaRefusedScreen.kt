package com.shayshankrathore.irishvisadate.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.URL_APPEALS_INFO
import com.shayshankrathore.irishvisadate.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val APPEAL_WINDOW_DAYS = 61L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaRefusedScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val today   = LocalDate.now()

    var refusalDate by remember { mutableStateOf<LocalDate?>(null) }
    var showPicker  by remember { mutableStateOf(false) }

    val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = refusalDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        },
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        refusalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showPicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Visa Refused", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back", color = IrishGreen) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF063320),
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Appeal section ────────────────────────────────────────────────
            AccentCard(title = "📋  APPEAL YOUR DECISION", accentColor = StatusRed) {
                Text(
                    "You can appeal within 2 months of your refusal date. Send your appeal to the Visa Appeals Officer with a detailed letter addressing each specific reason for refusal, plus any new supporting evidence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))

                if (refusalDate == null) {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        shape  = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                    ) { Text("Set refusal date to see deadline") }
                } else {
                    val deadline = refusalDate!!.plusDays(APPEAL_WINDOW_DAYS)
                    val daysLeft = ChronoUnit.DAYS.between(today, deadline)
                    val progress = ((APPEAL_WINDOW_DAYS - daysLeft).toFloat() / APPEAL_WINDOW_DAYS).coerceIn(0f, 1f)
                    val isExpired = daysLeft < 0

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(
                                if (isExpired) "Appeal window expired"
                                else "$daysLeft days left to appeal",
                                fontWeight = FontWeight.Bold,
                                color = if (daysLeft < 14) StatusRed else IrishGreenDark,
                            )
                            Text("Deadline: $deadline", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        TextButton(onClick = { showPicker = true }) { Text("Change", color = IrishGreen) }
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isExpired || daysLeft < 14) StatusRed else IrishGreen,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { openCustomTab(context, URL_APPEALS_INFO) },
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IrishGreen, contentColor = androidx.compose.ui.graphics.Color.White),
                ) { Text("🌐 Open appeals info") }
            }

            // ── Reapplication timeline ────────────────────────────────────────
            if (refusalDate != null) {
                val earliestReapply    = refusalDate!!.plusDays(90)
                val recommendedReapply = refusalDate!!.plusDays(180)
                val daysTo3Month  = ChronoUnit.DAYS.between(today, earliestReapply)
                val daysTo6Month  = ChronoUnit.DAYS.between(today, recommendedReapply)
                val pastEarliest  = today.isAfter(earliestReapply) || today.isEqual(earliestReapply)
                val pastRecommended = today.isAfter(recommendedReapply) || today.isEqual(recommendedReapply)

                AccentCard(title = "⏱️  REAPPLICATION TIMELINE", accentColor = IrishOrange) {
                    Text(
                        "Track when you can and should reapply for the best chance of success:",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary,
                    )
                    Spacer(Modifier.height(14.dp))

                    MilestoneRow(
                        emoji = if (pastEarliest) "✅" else "🔴",
                        title = "Earliest reapply (3 months)",
                        date  = earliestReapply,
                        note  = if (pastEarliest) "Reached — you can reapply now" else "In $daysTo3Month days",
                        color = if (pastEarliest) IrishGreen else StatusRed,
                    )
                    Spacer(Modifier.height(14.dp))
                    MilestoneRow(
                        emoji = if (pastRecommended) "✅" else if (pastEarliest) "🟡" else "🔘",
                        title = "Recommended reapply (6 months)",
                        date  = recommendedReapply,
                        note  = when {
                            pastRecommended -> "Reached — strong position to reapply"
                            pastEarliest    -> "In $daysTo6Month days — use this time to strengthen your case"
                            else            -> "In $daysTo6Month days"
                        },
                        color = if (pastRecommended) IrishGreen else if (pastEarliest) StatusAmber else TextSecondary,
                    )

                    Spacer(Modifier.height(12.dp))
                    val totalDays  = 180f
                    val elapsed    = ChronoUnit.DAYS.between(refusalDate, today).coerceIn(0, 180).toFloat()
                    val progress   = (elapsed / totalDays).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color    = if (pastRecommended) IrishGreen else if (pastEarliest) StatusAmber else StatusRed,
                        trackColor = DividerGreen,
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Refusal", style = MaterialTheme.typography.labelSmall, color = TextHint)
                        Text("3 months", style = MaterialTheme.typography.labelSmall, color = TextHint)
                        Text("6 months", style = MaterialTheme.typography.labelSmall, color = TextHint)
                    }
                }
            }

            // ── Re-application planner ────────────────────────────────────────
            AccentCard(title = "🔄  PLAN YOUR REAPPLICATION", accentColor = IrishOrange) {
                Text(
                    "You can submit a fresh application at any time. However, a quick reapplication with the same documents will likely be refused again. Use the time to build a stronger case.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))

                ReapplyTip("⏳", "Wait at least 3 months",
                    "Give yourself time to gather stronger evidence. A hasty reapplication without changes rarely succeeds.")
                ReapplyTip("📄", "Address each refusal reason",
                    "Read your refusal letter carefully. Write a detailed cover letter responding to every single reason stated.")
                ReapplyTip("💰", "Strengthen financial evidence",
                    "Provide 6 months of bank statements showing consistent balances. Explain any large deposits or gaps.")
                ReapplyTip("🏠", "Show ties to your home country",
                    "Evidence of employment, property ownership, business, or family commitments that ensure your return.")
                ReapplyTip("✈️", "Provide a clear travel purpose",
                    "A detailed itinerary, confirmed accommodation bookings, event tickets, or invitation letters help enormously.")

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = DividerGreen)
                Spacer(Modifier.height(12.dp))

                Text(
                    "PRE-APPLICATION CHECKLIST",
                    fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                    color = IrishOrange, letterSpacing = 0.8.sp,
                )
                Spacer(Modifier.height(10.dp))

                val checklistItems = listOf(
                    "Refusal reasons addressed in cover letter",
                    "6 months of bank statements included",
                    "Employment / income proof attached",
                    "Full itinerary and accommodation details",
                    "Return ticket or travel schedule",
                    "Ties to home country evidenced",
                    "Passport valid for trip duration",
                    "Correct visa fee paid",
                )
                val checkedItems = remember { mutableStateListOf<Int>() }
                checklistItems.forEachIndexed { idx, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = idx in checkedItems,
                            onCheckedChange = {
                                if (idx in checkedItems) checkedItems.remove(idx)
                                else checkedItems.add(idx)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = IrishGreen),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            item,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (idx in checkedItems) IrishGreen else TextSecondary,
                            fontWeight = if (idx in checkedItems) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
                if (checkedItems.size == checklistItems.size) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "✅ All items checked — you're ready to reapply!",
                        style = MaterialTheme.typography.bodySmall,
                        color = IrishGreen, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReapplyTip(emoji: String, title: String, body: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 13.sp)
            Text(body, style = MaterialTheme.typography.bodySmall, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun MilestoneRow(emoji: String, title: String, date: LocalDate, note: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 13.sp)
            Text(date.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", java.util.Locale.getDefault())), fontWeight = FontWeight.ExtraBold, color = color, fontSize = 16.sp)
            Text(note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

internal fun openCustomTab(context: Context, url: String) {
    CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(url))
}
