package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.*
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val DATE_FMT_CT = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private fun LocalDate.fmtCt() = format(DATE_FMT_CT)

data class ProcessingRecord(
    val appId: String,
    val embassyLabel: String,
    val embassyFlag: String,
    val visaTypeLabel: String,
    val submissionDate: LocalDate,
    val decisionDate: LocalDate,
    val status: String,
) {
    val calendarDays: Long get() = ChronoUnit.DAYS.between(submissionDate, decisionDate)
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTimesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val apps by AppPreferences.savedApplicationsFlow(context).collectAsState(initial = emptyList())

    val records = remember(apps) {
        apps.mapNotNull { app ->
            val sub = runCatching { LocalDate.parse(app.submissionDate) }.getOrNull() ?: return@mapNotNull null
            val dec = runCatching { LocalDate.parse(app.decisionDate) }.getOrNull()  ?: return@mapNotNull null
            if (!dec.isAfter(sub)) return@mapNotNull null
            ProcessingRecord(
                appId         = app.id,
                embassyLabel  = app.embassyLabel,
                embassyFlag   = app.embassyFlag,
                visaTypeLabel = app.visaTypeLabel,
                submissionDate = sub,
                decisionDate   = dec,
                status         = app.status,
            )
        }
    }

    // group by embassyId + visaType for aggregates
    val groups = remember(records) {
        records.groupBy { "${it.embassyFlag} ${it.embassyLabel} — ${it.visaTypeLabel}" }
    }

    // pick-a-date state for recording decision on an existing app
    var recordingApp  by remember { mutableStateOf<AppPreferences.SavedApplication?>(null) }
    var showPicker    by remember { mutableStateOf(false) }
    val todayMs = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        },
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false; recordingApp = null },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        recordingApp?.let { app ->
                            scope.launch {
                                AppPreferences.setApplicationDecisionDate(context, app.id, date.toString())
                            }
                        }
                    }
                    showPicker = false; recordingApp = null
                }) { Text("Save", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPicker = false; recordingApp = null }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Processing Times", fontWeight = FontWeight.ExtraBold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AccentCard(title = "📊  YOUR PROCESSING TIME DATA", accentColor = IrishGreen) {
                Text(
                    "Stats are built from your own saved applications that have an actual decision date recorded. The more outcomes you record, the more accurate your personal stats become.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${records.size} outcome${if (records.size == 1) "" else "s"} recorded on this device",
                    fontWeight = FontWeight.Bold,
                    color = IrishGreenDark,
                    fontSize = 14.sp,
                )
            }

            if (groups.isEmpty()) {
                AccentCard(title = "📭  NO DATA YET", accentColor = IrishOrange) {
                    Text(
                        "No decision dates recorded yet. Once you receive a visa decision, go to My Apps and tap \"Record decision date\" on any saved application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                }
            } else {
                groups.entries.sortedByDescending { it.value.size }.forEach { (groupKey, recs) ->
                    val days     = recs.map { it.calendarDays }
                    val avg      = days.average().roundToInt()
                    val minDays  = days.min()
                    val maxDays  = days.max()
                    val granted  = recs.count { it.status == "GRANTED" }
                    val refused  = recs.count { it.status == "REFUSED" }

                    AccentCard(title = groupKey, accentColor = IrishGreen) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            StatBlock("${recs.size}", "records")
                            StatBlock("${avg}d", "avg days")
                            StatBlock("${minDays}d", "fastest")
                            StatBlock("${maxDays}d", "slowest")
                        }
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = DividerGreen)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth()) {
                            if (granted > 0) {
                                Text("✅ $granted granted", style = MaterialTheme.typography.labelSmall, color = IrishGreen, modifier = Modifier.padding(end = 12.dp))
                            }
                            if (refused > 0) {
                                Text("❌ $refused refused", style = MaterialTheme.typography.labelSmall, color = StatusRed)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        recs.sortedByDescending { it.decisionDate }.forEach { rec ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(if (rec.status == "GRANTED") "✅" else if (rec.status == "REFUSED") "❌" else "🕐", fontSize = 13.sp)
                                Spacer(Modifier.width(6.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Submitted ${rec.submissionDate.fmtCt()}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text("Decision ${rec.decisionDate.fmtCt()}", style = MaterialTheme.typography.labelSmall, color = IrishGreenDark)
                                }
                                Text("${rec.calendarDays}d", fontWeight = FontWeight.Bold, color = IrishGreen, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // record decision date for apps that don't have one yet
            val pendingApps = apps.filter {
                it.decisionDate.isBlank() &&
                it.status != "PENDING" || (it.decisionDate.isBlank() && it.status == "PENDING")
            }.filter { it.submissionDate.isNotBlank() }

            val appsNeedingDate = apps.filter {
                it.decisionDate.isBlank() && it.submissionDate.isNotBlank()
            }

            if (appsNeedingDate.isNotEmpty()) {
                AccentCard(title = "📝  RECORD DECISION DATES", accentColor = IrishOrange) {
                    Text(
                        "Record actual decision dates on your saved applications to grow your stats:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(10.dp))
                    appsNeedingDate.forEach { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(app.embassyFlag, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(app.embassyLabel, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IrishGreenDark)
                                Text("${app.visaTypeLabel} • ${app.submissionDate}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            OutlinedButton(
                                onClick = { recordingApp = app; showPicker = true },
                                shape  = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IrishGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text("Add date", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        HorizontalDivider(color = DividerGreen)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = IrishGreenDark)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}
