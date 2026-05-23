package com.example.irishvisaexpecteddate.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.irishvisaexpecteddate.COURIER_RETURN_DAYS
import com.example.irishvisaexpecteddate.JOIN_FAMILY_MAX_DAYS
import com.example.irishvisaexpecteddate.JOIN_FAMILY_MIN_DAYS
import com.example.irishvisaexpecteddate.SHORT_STAY_MAX_DAYS
import com.example.irishvisaexpecteddate.SHORT_STAY_MIN_DAYS
import com.example.irishvisaexpecteddate.STUDY_MAX_DAYS
import com.example.irishvisaexpecteddate.STUDY_MIN_DAYS
import com.example.irishvisaexpecteddate.URL_DECISIONS_PAGE
import com.example.irishvisaexpecteddate.VAC_DELHI_TRANSIT_DAYS
import com.example.irishvisaexpecteddate.VAC_OTHER_TRANSIT_DAYS
import com.example.irishvisaexpecteddate.addWorkingDays
import com.example.irishvisaexpecteddate.workingDaysBetween
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Domain types ──────────────────────────────────────────────────────────────

enum class VacLocation(val label: String, val transitDays: Int) {
    DELHI("Delhi", VAC_DELHI_TRANSIT_DAYS),
    OTHER("Mumbai / Bengaluru / Chennai / Kolkata", VAC_OTHER_TRANSIT_DAYS),
}

enum class VisaType(val label: String, val minDays: Int, val maxDays: Int) {
    SHORT_STAY("Short Stay C\n(Tourist/Business)", SHORT_STAY_MIN_DAYS, SHORT_STAY_MAX_DAYS),
    STUDY("Long Stay D\n(Study)", STUDY_MIN_DAYS, STUDY_MAX_DAYS),
    JOIN_FAMILY("Long Stay D\n(Join Family)", JOIN_FAMILY_MIN_DAYS, JOIN_FAMILY_MAX_DAYS),
}

private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

private fun LocalDate.fmt(): String = format(DATE_FMT)
private fun plural(n: Long) = if (n == 1L) "" else "s"

// ── Screen ────────────────────────────────────────────────────────────────────

enum class AppScreen { TRACKER, GRANTED, REFUSED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaTrackerScreen(onNavigate: (AppScreen) -> Unit) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    var submissionDate by remember { mutableStateOf<LocalDate?>(null) }
    var vacLocation by remember { mutableStateOf(VacLocation.DELHI) }
    var visaType by remember { mutableStateOf(VisaType.SHORT_STAY) }
    var showDatePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNoDecisionSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = submissionDate
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        submissionDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = pickerState) }
    }

    if (showNoDecisionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNoDecisionSheet = false },
            sheetState = sheetState,
        ) {
            NoDecisionSheet(
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showNoDecisionSheet = false
                    }
                },
                onOpenDecisions = { openCustomTab(context, URL_DECISIONS_PAGE) },
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Irish Visa Tracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Submission date ───────────────────────────────────────────────
            SectionCard("Application Submitted") {
                if (submissionDate != null) {
                    Text(
                        text = submissionDate!!.fmt(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(if (submissionDate == null) "Pick submission date" else "Change date")
                }
            }

            // ── VAC location ──────────────────────────────────────────────────
            SectionCard("VAC Location") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    VacLocation.entries.forEachIndexed { i, loc ->
                        SegmentedButton(
                            selected = vacLocation == loc,
                            onClick = { vacLocation = loc },
                            shape = SegmentedButtonDefaults.itemShape(i, VacLocation.entries.size),
                            label = { Text(loc.label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
                Text(
                    text = "${vacLocation.transitDays} working day${plural(vacLocation.transitDays.toLong())} transit to Embassy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            // ── Visa type ─────────────────────────────────────────────────────
            SectionCard("Visa Type") {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    VisaType.entries.forEachIndexed { i, type ->
                        SegmentedButton(
                            selected = visaType == type,
                            onClick = { visaType = type },
                            shape = SegmentedButtonDefaults.itemShape(i, VisaType.entries.size),
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            // ── Decision window (shown once date is chosen) ───────────────────
            if (submissionDate != null) {
                val submission = submissionDate!!
                val transit = vacLocation.transitDays

                // Day 1 = lodgment day (VAC transit counts from day 1)
                val embassyReceiveDate = submission.addWorkingDays(transit)
                val earliestDecision = embassyReceiveDate.addWorkingDays(visaType.minDays)
                val latestDecision = embassyReceiveDate.addWorkingDays(visaType.maxDays)

                val totalWindowDays = workingDaysBetween(earliestDecision, latestDecision)
                val elapsedSinceEarliest = workingDaysBetween(earliestDecision, today)
                    .coerceAtLeast(0L)
                val elapsedSinceSubmission = workingDaysBetween(submission, today)
                    .coerceAtLeast(0L)

                val isBeforeWindow = today.isBefore(earliestDecision)
                val isOverdue = today.isAfter(latestDecision)
                val isInWindow = !isBeforeWindow && !isOverdue

                val windowProgress = when {
                    isBeforeWindow -> 0f
                    isOverdue -> 1f
                    totalWindowDays > 0 ->
                        (elapsedSinceEarliest.toFloat() / totalWindowDays).coerceIn(0f, 1f)
                    else -> 1f
                }

                DecisionWindowCard(
                    today = today,
                    earliestDecision = earliestDecision,
                    latestDecision = latestDecision,
                    elapsedSinceSubmission = elapsedSinceSubmission,
                    windowProgress = windowProgress,
                    isBeforeWindow = isBeforeWindow,
                    isOverdue = isOverdue,
                    isInWindow = isInWindow,
                    transit = transit,
                )

                // ── Status update section ─────────────────────────────────────
                SectionCard("Got an Update?") {
                    Text(
                        text = "Let us know the outcome of your application:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigate(AppScreen.GRANTED) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) { Text("✅  I got my visa!") }

                        Button(
                            onClick = { onNavigate(AppScreen.REFUSED) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                        ) { Text("❌  My visa was refused") }

                        OutlinedButton(
                            onClick = { showNoDecisionSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("🕐  No decision yet") }
                    }
                }
            }
        }
    }
}

// ── Decision window card ──────────────────────────────────────────────────────

@Composable
private fun DecisionWindowCard(
    today: LocalDate,
    earliestDecision: LocalDate,
    latestDecision: LocalDate,
    elapsedSinceSubmission: Long,
    windowProgress: Float,
    isBeforeWindow: Boolean,
    isOverdue: Boolean,
    isInWindow: Boolean,
    transit: Int,
) {
    val containerColor = when {
        isOverdue -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val overdueAmber = MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "EXPECTED DECISION WINDOW",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Expect a decision between",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${earliestDecision.fmt()}  —  ${latestDecision.fmt()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isOverdue) overdueAmber else MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(includes $transit working day${plural(transit.toLong())} VAC transit)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { windowProgress },
                modifier = Modifier.fillMaxWidth(),
                color = if (isOverdue) overdueAmber else MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "$elapsedSinceSubmission",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "working days elapsed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val (value, label) = when {
                        isBeforeWindow -> {
                            val daysToEarliest = workingDaysBetween(today, earliestDecision)
                            "$daysToEarliest" to "days until earliest"
                        }
                        isOverdue -> {
                            val overdueDays = workingDaysBetween(latestDecision, today)
                            "$overdueDays" to "days overdue"
                        }
                        else -> {
                            val remaining = workingDaysBetween(today, latestDecision)
                            "$remaining" to "days left in window"
                        }
                    }
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) overdueAmber else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Plus ~$COURIER_RETURN_DAYS working days for return courier " +
                       "(Embassy → Delhi VAC → your address via Blue Dart)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isOverdue) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    ),
                ) {
                    Text(
                        text = "Your application may be overdue — you can email the Visa Office for a status update.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

// ── No Decision bottom sheet ──────────────────────────────────────────────────

@Composable
private fun NoDecisionSheet(onClose: () -> Unit, onOpenDecisions: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "🕐  No Decision Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "The Embassy publishes a daily decisions list around 11:00 IST. Check whether your name appears:",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onOpenDecisions,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open Visa Decisions Page")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Close")
        }
    }
}

// ── Shared card wrapper ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}
