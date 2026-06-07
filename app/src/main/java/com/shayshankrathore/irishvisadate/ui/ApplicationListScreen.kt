package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import com.shayshankrathore.irishvisadate.ALL_EMBASSIES
import com.shayshankrathore.irishvisadate.AppPreferences
import com.shayshankrathore.irishvisadate.AppPreferences.SavedApplication
import com.shayshankrathore.irishvisadate.addWorkingDays
import com.shayshankrathore.irishvisadate.workingDaysBetween
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationListScreen(
    onBack: () -> Unit,
    onLoadApplication: (SavedApplication) -> Unit,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val appsFlow = remember(context) { AppPreferences.savedApplicationsFlow(context) }
    val apps by appsFlow.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("My Applications", fontWeight = FontWeight.ExtraBold) },
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
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📁", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No saved applications yet",
                        fontWeight = FontWeight.SemiBold,
                        color = IrishGreenDark,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Use the \"My Apps\" button on the tracker\nto save your current application.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(apps, key = { it.id }) { app ->
                    ApplicationCard(
                        app = app,
                        onLoad = { onLoadApplication(app) },
                        onDelete = { scope.launch { AppPreferences.deleteApplication(context, app.id) } },
                    )
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationCard(
    app: SavedApplication,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
) {
    val context     = androidx.compose.ui.platform.LocalContext.current
    val scope       = rememberCoroutineScope()
    val today       = LocalDate.now()
    var showHistory by remember { mutableStateOf(false) }
    var showNotes   by remember { mutableStateOf(false) }
    var editNotes   by remember { mutableStateOf(app.notes) }
    var showDecisionPicker by remember { mutableStateOf(false) }

    val todayMs = today.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    val decisionPickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        },
    )

    if (showDecisionPicker) {
        DatePickerDialog(
            onDismissRequest = { showDecisionPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    decisionPickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        scope.launch { AppPreferences.setApplicationDecisionDate(context, app.id, date.toString()) }
                    }
                    showDecisionPicker = false
                }) { Text("Save", color = IrishGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDecisionPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = decisionPickerState) }
    }

    if (showNotes) {
        AlertDialog(
            onDismissRequest = { showNotes = false },
            title = { Text("Application Notes", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editNotes,
                    onValueChange = { editNotes = it },
                    label = { Text("Your notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IrishGreen, focusedLabelColor = IrishGreen,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { AppPreferences.updateApplicationNotes(context, app.id, editNotes) }
                    showNotes = false
                }) { Text("Save", color = IrishGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showNotes = false }) { Text("Cancel") } },
        )
    }
    val embassy  = ALL_EMBASSIES.find { it.id == app.embassyId }
    val vac      = embassy?.vacOptions?.find { it.label == app.vacLabel }
    val visaType = runCatching { VisaType.valueOf(app.visaTypeName) }.getOrNull()

    val decisionInfo: String? = if (embassy != null && vac != null && visaType != null) {
        runCatching {
            val submission    = LocalDate.parse(app.submissionDate)
            val receive       = submission.addWorkingDays(vac.transitDays, embassy.holidays)
            val earliest      = receive.addWorkingDays(visaType.minDays, embassy.holidays)
            val latest        = receive.addWorkingDays(visaType.maxDays, embassy.holidays)
            val isOverdue     = today.isAfter(latest)
            val isBeforeWin   = today.isBefore(earliest)
            when {
                app.status == "GRANTED"  -> "✅ Visa granted"
                app.status == "REFUSED"  -> "❌ Visa refused"
                isOverdue                -> "⚠️ ${workingDaysBetween(latest, today, embassy.holidays)} days overdue"
                isBeforeWin              -> "⏳ Window opens in ${workingDaysBetween(today, earliest, embassy.holidays)} days"
                else                     -> "🗓 ${workingDaysBetween(today, latest, embassy.holidays)} days left in window"
            }
        }.getOrNull()
    } else null

    val statusColor = when (app.status) {
        "GRANTED" -> IrishGreen
        "REFUSED" -> StatusRed
        else      -> IrishOrange
    }

    Card(
        onClick = onLoad,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(modifier = Modifier.heightIn(min = 72.dp)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(statusColor, statusColor.copy(alpha = 0.35f))),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(app.embassyFlag, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        app.embassyLabel,
                        fontWeight = FontWeight.Bold,
                        color = IrishGreenDark,
                        fontSize = 15.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${app.visaTypeLabel}  •  ${app.vacLabel}  •  Submitted ${app.submissionDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
                if (decisionInfo != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(decisionInfo, style = MaterialTheme.typography.bodySmall,
                        color = IrishGreenDark, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tap to load into tracker",
                    style = MaterialTheme.typography.labelSmall,
                    color = IrishGreen,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onDelete) { Text("🗑️", fontSize = 18.sp) }
                TextButton(onClick = { showHistory = !showHistory }) {
                    Text(if (showHistory) "▲" else "📜", fontSize = 14.sp, color = IrishGreen)
                }
                TextButton(onClick = { editNotes = app.notes; showNotes = true }) {
                    Text("✏️", fontSize = 14.sp)
                }
            }
        }

        // Notes row
        if (app.notes.isNotBlank()) {
            HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(horizontal = 14.dp))
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                Text("📝 ", fontSize = 12.sp)
                Text(app.notes, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        // Decision date row
        if (app.decisionDate.isNotBlank()) {
            HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(horizontal = 14.dp))
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("📅 Decision: ", style = MaterialTheme.typography.labelSmall, color = TextHint)
                Text(app.decisionDate, style = MaterialTheme.typography.labelSmall, color = IrishGreenDark, fontWeight = FontWeight.SemiBold)
            }
        } else {
            HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(horizontal = 14.dp))
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Record actual decision date: ", style = MaterialTheme.typography.labelSmall, color = TextHint, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = { showDecisionPicker = true },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                ) { Text("+ Add date", color = IrishGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            }
        }

        if (showHistory && app.statusHistory.isNotBlank()) {
            HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(horizontal = 14.dp))
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("HISTORY", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = IrishGreen, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(4.dp))
                app.statusHistory.split("|").forEach { entry ->
                    Text(
                        "• $entry",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                    )
                }
            }
        }
    }
}
