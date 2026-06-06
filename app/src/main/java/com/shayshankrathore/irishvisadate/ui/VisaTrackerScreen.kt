package com.shayshankrathore.irishvisadate.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.provider.CalendarContract
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shayshankrathore.irishvisadate.AppPreferences.SavedApplication
import com.shayshankrathore.irishvisadate.NotificationWorker
import java.util.concurrent.TimeUnit
import com.shayshankrathore.irishvisadate.ALL_EMBASSIES
import com.shayshankrathore.irishvisadate.AppPreferences
import com.shayshankrathore.irishvisadate.Embassy
import com.shayshankrathore.irishvisadate.JOIN_FAMILY_MAX_DAYS
import com.shayshankrathore.irishvisadate.JOIN_FAMILY_MIN_DAYS
import com.shayshankrathore.irishvisadate.SHORT_STAY_MAX_DAYS
import com.shayshankrathore.irishvisadate.SHORT_STAY_MIN_DAYS
import com.shayshankrathore.irishvisadate.STUDY_MAX_DAYS
import com.shayshankrathore.irishvisadate.STUDY_MIN_DAYS
import com.shayshankrathore.irishvisadate.TRANSIT_MAX_DAYS
import com.shayshankrathore.irishvisadate.TRANSIT_MIN_DAYS
import com.shayshankrathore.irishvisadate.VacOption
import com.shayshankrathore.irishvisadate.WORK_PERMIT_MAX_DAYS
import com.shayshankrathore.irishvisadate.WORK_PERMIT_MIN_DAYS
import com.shayshankrathore.irishvisadate.WORKING_HOLIDAY_MAX_DAYS
import com.shayshankrathore.irishvisadate.WORKING_HOLIDAY_MIN_DAYS
import com.shayshankrathore.irishvisadate.addWorkingDays
import com.shayshankrathore.irishvisadate.workingDaysBetween
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Domain types ──────────────────────────────────────────────────────────────

enum class VisaType(val label: String, val minDays: Int, val maxDays: Int) {
    SHORT_STAY("Short Stay C", SHORT_STAY_MIN_DAYS, SHORT_STAY_MAX_DAYS),
    STUDY("Study (D)", STUDY_MIN_DAYS, STUDY_MAX_DAYS),
    JOIN_FAMILY("Join Family (D)", JOIN_FAMILY_MIN_DAYS, JOIN_FAMILY_MAX_DAYS),
    WORK_PERMIT("Critical Skills / Work (D)", WORK_PERMIT_MIN_DAYS, WORK_PERMIT_MAX_DAYS),
    WORKING_HOLIDAY("Working Holiday (D)", WORKING_HOLIDAY_MIN_DAYS, WORKING_HOLIDAY_MAX_DAYS),
    TRANSIT("Transit (A)", TRANSIT_MIN_DAYS, TRANSIT_MAX_DAYS),
}

private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

private fun LocalDate.fmt(): String = format(DATE_FMT)
private fun plural(n: Long) = if (n == 1L) "" else "s"

enum class AppScreen { TRACKER, GRANTED, REFUSED, CHECKLIST, DECISIONS_WEB, APP_LIST }

// ── Harp watermark ────────────────────────────────────────────────────────────
private fun DrawScope.drawHarp(alpha: Float = 0.13f) {
    val c  = Color.White.copy(alpha = alpha)
    val sw = 2.8f
    val w  = size.width
    val h  = size.height

    val neck = Path().apply {
        moveTo(w * 0.60f, h * 0.04f)
        cubicTo(w * 1.00f, h * 0.18f, w * 0.88f, h * 0.62f, w * 0.58f, h * 0.93f)
    }
    drawPath(neck, c, style = Stroke(width = sw, cap = StrokeCap.Round))

    drawLine(c, androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.10f),
                androidx.compose.ui.geometry.Offset(w * 0.08f, h * 0.93f), sw)

    val box = Path().apply {
        moveTo(w * 0.08f, h * 0.93f)
        cubicTo(w * 0.22f, h * 1.04f, w * 0.44f, h * 1.04f, w * 0.58f, h * 0.93f)
    }
    drawPath(box, c, style = Stroke(width = sw, cap = StrokeCap.Round))

    for (i in 0..7) {
        val t = i / 7f
        val sy = h * (0.14f + t * 0.71f)
        val nx = w * (0.58f + (1f - t) * 0.06f)
        val ny = h * (0.04f + t * 0.89f)
        drawLine(c.copy(alpha = alpha * 0.75f),
            androidx.compose.ui.geometry.Offset(w * 0.08f, sy),
            androidx.compose.ui.geometry.Offset(nx, ny), sw * 0.55f)
    }
}

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun IrishHeader(onHelpClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF063320), Color(0xFF0F6B40))))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(110.dp)
                .padding(end = 8.dp)
        ) { drawHarp() }

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("☘️", fontSize = 26.sp) }

                Spacer(Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Irish Visa Tracker",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.3).sp,
                    )
                    Text(
                        text = "Embassy Decision Calculator  🇮🇪",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 12.sp,
                        letterSpacing = 0.2.sp,
                    )
                }

                Spacer(Modifier.weight(1f))

                Surface(
                    onClick = onHelpClick,
                    shape = RoundedCornerShape(9.dp),
                    color = Color.White.copy(alpha = 0.18f),
                    modifier = Modifier.size(34.dp),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text("?", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().height(5.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight().background(IrishGreen))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color.White))
                Box(Modifier.weight(1f).fillMaxHeight().background(IrishOrange))
            }
        }
    }
}

// ── Main screen ───────────────────────────────────────────────────────────────
@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaTrackerScreen(
    onNavigate: (AppScreen) -> Unit,
    onOpenChecklist: (VisaType) -> Unit,
    onOpenDecisions: (String) -> Unit,
) {
    val context = LocalContext.current
    val today   = LocalDate.now()
    val todayMs = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    var selectedEmbassy by remember { mutableStateOf(ALL_EMBASSIES[0]) }
    var selectedVac     by remember { mutableStateOf(ALL_EMBASSIES[0].vacOptions[0]) }
    var submissionDate  by remember { mutableStateOf<LocalDate?>(null) }
    var visaType        by remember { mutableStateOf(VisaType.SHORT_STAY) }
    var showDatePicker  by remember { mutableStateOf(false) }
    var embassyExpanded by remember { mutableStateOf(false) }
    var showHowToUse    by remember { mutableStateOf(false) }
    var stateRestored   by remember { mutableStateOf(false) }

    val prefFlow   = remember(context) { AppPreferences.flow(context) }
    val savedState by prefFlow.collectAsState(initial = null)

    // Restore persisted state once on first load
    LaunchedEffect(savedState) {
        if (!stateRestored && savedState != null) {
            savedState!!.embassyId?.let { id ->
                ALL_EMBASSIES.find { it.id == id }?.also { embassy ->
                    selectedEmbassy = embassy
                    savedState!!.vacLabel?.let { label ->
                        embassy.vacOptions.find { it.label == label }?.let { selectedVac = it }
                    }
                }
            }
            savedState!!.submissionDate?.let { dateStr ->
                runCatching { LocalDate.parse(dateStr) }.getOrNull()?.let { submissionDate = it }
            }
            savedState!!.visaTypeName?.let { name ->
                runCatching { VisaType.valueOf(name) }.getOrNull()?.let { visaType = it }
            }
            stateRestored = true
        }
    }

    // Persist state whenever the user changes anything
    LaunchedEffect(selectedEmbassy, selectedVac, submissionDate, visaType) {
        if (stateRestored) {
            AppPreferences.save(
                context        = context,
                embassyId      = selectedEmbassy.id,
                vacLabel       = selectedVac.label,
                submissionDate = submissionDate?.toString(),
                visaTypeName   = visaType.name,
            )
        }
    }

    // Schedule / reschedule notifications when inputs change
    LaunchedEffect(selectedEmbassy, selectedVac, submissionDate, visaType) {
        if (!stateRestored) return@LaunchedEffect
        val wm = WorkManager.getInstance(context)
        wm.cancelAllWorkByTag(NotificationWorker.WORK_TAG)
        val sub = submissionDate ?: return@LaunchedEffect
        val holidays = selectedEmbassy.holidays
        val receive  = sub.addWorkingDays(selectedVac.transitDays, holidays)
        val earliest = receive.addWorkingDays(visaType.minDays, holidays)
        val latest   = receive.addWorkingDays(visaType.maxDays, holidays)
        val winDays  = workingDaysBetween(earliest, latest, holidays)
        val now      = System.currentTimeMillis()
        fun schedule(target: LocalDate, type: String) {
            val delay = target.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() - now
            if (delay <= 0) return
            wm.enqueue(
                OneTimeWorkRequestBuilder<NotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(workDataOf(
                        NotificationWorker.KEY_TYPE    to type,
                        NotificationWorker.KEY_EMBASSY to selectedEmbassy.label,
                        NotificationWorker.KEY_VISA    to visaType.label,
                    ))
                    .addTag(NotificationWorker.WORK_TAG)
                    .build()
            )
        }
        schedule(earliest.minusDays(1), NotificationWorker.TYPE_WINDOW_OPENS)
        if (winDays > 2) schedule(earliest.plusDays(winDays / 2), NotificationWorker.TYPE_MIDPOINT)
        schedule(latest.plusDays(1), NotificationWorker.TYPE_OVERDUE)
    }

    val sheetState          = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val howToUseSheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNoDecision      by remember { mutableStateOf(false) }
    val scope               = rememberCoroutineScope()

    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = submissionDate
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        submissionDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
        ) { DatePicker(state = pickerState) }
    }

    if (showNoDecision) {
        ModalBottomSheet(
            onDismissRequest = { showNoDecision = false },
            sheetState = sheetState,
            containerColor = SurfaceCard,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(DividerGreen, RoundedCornerShape(2.dp))
                )
            }
        ) {
            NoDecisionSheet(
                onClose = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showNoDecision = false
                    }
                },
                onOpenDecisions = { onOpenDecisions(selectedEmbassy.decisionsUrl) },
            )
        }
    }

    if (showHowToUse) {
        ModalBottomSheet(
            onDismissRequest = { showHowToUse = false },
            sheetState = howToUseSheetState,
            containerColor = SurfaceCard,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(DividerGreen, RoundedCornerShape(2.dp))
                )
            }
        ) {
            HowToUseSheet(
                onClose = {
                    scope.launch { howToUseSheetState.hide() }.invokeOnCompletion {
                        showHowToUse = false
                    }
                }
            )
        }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = { IrishHeader(onHelpClick = { showHowToUse = true }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            // ── Embassy selector ──────────────────────────────────────────
            AccentCard(title = "🌍  IRISH EMBASSY", accentColor = IrishGreen) {
                ExposedDropdownMenuBox(
                    expanded = embassyExpanded,
                    onExpandedChange = { embassyExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = "${selectedEmbassy.flag}  ${selectedEmbassy.label}",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = embassyExpanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IrishGreen,
                            unfocusedBorderColor = IrishGreen.copy(alpha = 0.45f),
                            focusedTextColor = IrishGreenDark,
                            unfocusedTextColor = IrishGreenDark,
                            focusedTrailingIconColor = IrishGreen,
                            unfocusedTrailingIconColor = IrishGreenDark,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                    )
                    ExposedDropdownMenu(
                        expanded = embassyExpanded,
                        onDismissRequest = { embassyExpanded = false },
                    ) {
                        ALL_EMBASSIES.forEach { embassy ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${embassy.flag}  ${embassy.label}",
                                        fontWeight = if (embassy == selectedEmbassy)
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (embassy == selectedEmbassy)
                                            IrishGreenDark else TextSecondary,
                                    )
                                },
                                onClick = {
                                    selectedEmbassy = embassy
                                    selectedVac = embassy.vacOptions[0]
                                    embassyExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            // ── Submission date ───────────────────────────────────────────
            AccentCard(title = "📅  APPLICATION SUBMITTED", accentColor = IrishGreen) {
                if (submissionDate != null) {
                    Text(
                        text = submissionDate!!.fmt(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = IrishGreenDark,
                        letterSpacing = (-0.3).sp,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                ) {
                    Text(
                        if (submissionDate == null) "Pick submission date" else "Change date",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── VAC location ──────────────────────────────────────────────
            AccentCard(title = "📍  VISA APPLICATION CENTRE (VAC) LOCATION", accentColor = IrishGreen) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedEmbassy.vacOptions.forEach { vac ->
                        val selected = selectedVac == vac
                        Surface(
                            onClick = { selectedVac = vac },
                            shape   = RoundedCornerShape(10.dp),
                            border  = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selected) IrishGreen else DividerGreen,
                            ),
                            color = if (selected) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick  = { selectedVac = vac },
                                    colors   = RadioButtonDefaults.colors(selectedColor = IrishGreen),
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text       = vac.label,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                        fontSize   = 15.sp,
                                        color      = if (selected) IrishGreenDark else TextSecondary,
                                    )
                                    Text(
                                        text  = "${vac.cities} — ${vac.transitDays} working day${plural(vac.transitDays.toLong())} transit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextHint,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Visa type ─────────────────────────────────────────────────
            AccentCard(title = "🛂  VISA TYPE", accentColor = IrishGreen) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VisaType.entries.forEach { type ->
                        val selected = visaType == type
                        Surface(
                            onClick = { visaType = type },
                            shape   = RoundedCornerShape(10.dp),
                            border  = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (selected) IrishGreen else DividerGreen,
                            ),
                            color = if (selected) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick  = { visaType = type },
                                    colors   = RadioButtonDefaults.colors(selectedColor = IrishGreen),
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text       = type.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    fontSize   = 15.sp,
                                    color      = if (selected) IrishGreenDark else TextSecondary,
                                )
                            }
                        }
                    }
                }
            }

            // ── Decision window ───────────────────────────────────────────
            if (submissionDate != null) {
                val submission     = submissionDate!!
                val holidays       = selectedEmbassy.holidays
                val transit        = selectedVac.transitDays
                val embassyReceive    = submission.addWorkingDays(transit, holidays)
                val earliest         = embassyReceive.addWorkingDays(visaType.minDays, holidays)
                val latest           = embassyReceive.addWorkingDays(visaType.maxDays, holidays)
                val passportReturn   = latest.addWorkingDays(selectedEmbassy.courierDays, holidays)

                val windowDays    = workingDaysBetween(earliest, latest, holidays)
                val elapsed       = workingDaysBetween(earliest, today, holidays).coerceAtLeast(0L)
                val elapsedSub    = workingDaysBetween(submission, today, holidays).coerceAtLeast(0L)
                val isBeforeWindow = today.isBefore(earliest)
                val isOverdue      = today.isAfter(latest)
                val isInWindow     = !isBeforeWindow && !isOverdue
                val progress       = when {
                    isBeforeWindow -> 0f
                    isOverdue      -> 1f
                    windowDays > 0 -> (elapsed.toFloat() / windowDays).coerceIn(0f, 1f)
                    else           -> 1f
                }

                DecisionWindowCard(
                    today = today, earliest = earliest, latest = latest,
                    elapsedSub = elapsedSub, windowProgress = progress,
                    isBeforeWindow = isBeforeWindow, isOverdue = isOverdue,
                    isInWindow = isInWindow, transit = transit,
                    courierNote = selectedEmbassy.courierNote,
                    passportReturnDate = passportReturn,
                    courierDays = selectedEmbassy.courierDays,
                )

                // ── Quick actions ─────────────────────────────────────────
                AccentCard(title = "⚡  QUICK ACTIONS", accentColor = IrishGreen) {
                    val shareText = buildString {
                        append("🇮🇪 Irish Visa Application\n\n")
                        append("Type: ${visaType.label}\n")
                        append("Embassy: ${selectedEmbassy.label}\n")
                        append("Submitted: ${submissionDate!!.fmt()}\n\n")
                        append("Decision window: ${earliest.fmt()} – ${latest.fmt()}\n")
                        append("Passport back by: ~${passportReturn.fmt()}\n\n")
                        append("via Irish Visa Tracker")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }, "Share"
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                        ) { Text("📤 Share", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }

                        OutlinedButton(
                            onClick = {
                                context.startActivity(
                                    Intent(Intent.ACTION_INSERT).apply {
                                        data = CalendarContract.Events.CONTENT_URI
                                        putExtra(CalendarContract.Events.TITLE, "Irish Visa Decision Window")
                                        putExtra(CalendarContract.Events.DESCRIPTION,
                                            "Visa: ${visaType.label}\nEmbassy: ${selectedEmbassy.label}\nExpected: ${earliest.fmt()} – ${latest.fmt()}")
                                        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                            earliest.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                                        putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                            latest.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                                        putExtra(CalendarContract.Events.ALL_DAY, true)
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                        ) { Text("📅 Calendar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = { onOpenChecklist(visaType) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                        ) { Text("📋 Checklist", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }

                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.APP_LIST) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                        ) { Text("📁 My Apps", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                AppPreferences.saveApplication(
                                    context,
                                    SavedApplication(
                                        id             = AppPreferences.newApplicationId(),
                                        embassyId      = selectedEmbassy.id,
                                        embassyLabel   = selectedEmbassy.label,
                                        embassyFlag    = selectedEmbassy.flag,
                                        vacLabel       = selectedVac.label,
                                        submissionDate = submissionDate!!.toString(),
                                        visaTypeName   = visaType.name,
                                        visaTypeLabel  = visaType.label,
                                        savedAt        = LocalDate.now().toString(),
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IrishGreen, contentColor = Color.White),
                    ) {
                        Text("💾  Save Application", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // ── Status update ─────────────────────────────────────────
                AccentCard(title = "🔔  GOT AN UPDATE?", accentColor = IrishOrange) {
                    Text(
                        text = "Let us know the outcome of your application:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick  = { onNavigate(AppScreen.GRANTED) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = IrishGreen, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        ) {
                            Text("✅  I got my visa!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Button(
                            onClick  = { onNavigate(AppScreen.REFUSED) },
                            modifier = Modifier
                                .fillMaxWidth().height(52.dp)
                                .border(1.5.dp, StatusRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = StatusRedLight, contentColor = StatusRed),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text("❌  My visa was refused", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                        OutlinedButton(
                            onClick = { showNoDecision = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            border   = androidx.compose.foundation.BorderStroke(
                                1.5.dp, IrishGreen.copy(alpha = 0.6f)),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreenDark),
                        ) {
                            Text("🕐  No decision yet", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Decision window card ──────────────────────────────────────────────────────
@Composable
private fun DecisionWindowCard(
    today: LocalDate, earliest: LocalDate, latest: LocalDate,
    elapsedSub: Long, windowProgress: Float,
    isBeforeWindow: Boolean, isOverdue: Boolean, isInWindow: Boolean,
    transit: Int, courierNote: String,
    passportReturnDate: LocalDate, courierDays: Int,
) {
    val bgGradient = when {
        isOverdue  -> Brush.linearGradient(listOf(Color(0xFFFFF8E1), Color(0xFFFFECB3)))
        isInWindow -> Brush.linearGradient(listOf(Color(0xFFE8F8EF), Color(0xFFFFF9F0)))
        else       -> Brush.linearGradient(listOf(Color(0xFFEEFAF3), Color(0xFFE8F7EE)))
    }
    val accentColor = if (isOverdue) StatusAmber else IrishGreen
    val dateColor   = if (isOverdue) StatusAmber else IrishGreenDark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgGradient)
            .border(1.5.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(listOf(IrishGreen, IrishOrange)),
                    RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                )
        )

        Column(modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🗓", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "EXPECTED DECISION WINDOW",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = IrishGreenDark,
                    letterSpacing = 1.sp,
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("Expect a decision between", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${earliest.fmt()}  —  ${latest.fmt()}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = dateColor,
                letterSpacing = (-0.3).sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "(includes $transit working day${plural(transit.toLong())} VAC transit)",
                style = MaterialTheme.typography.labelSmall,
                color = TextHint,
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerGreen, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📬", fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Passport back by ~${passportReturnDate.fmt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = IrishGreenDark,
                    )
                    Text(
                        text = "(includes $courierDays working day${plural(courierDays.toLong())} courier return)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextHint,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(5.dp)).background(DividerGreen)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(windowProgress).fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(IrishGreen, if (isOverdue) StatusAmber else IrishOrange)
                            ),
                            RoundedCornerShape(5.dp)
                        )
                )
            }

            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatChip("$elapsedSub", "working days\nelapsed", IrishGreen)
                val (v, l) = when {
                    isBeforeWindow -> workingDaysBetween(today, earliest).toString() to "days until\nearliest"
                    isOverdue      -> workingDaysBetween(latest, today).toString() to "days\noverdue"
                    else           -> workingDaysBetween(today, latest).toString() to "days left\nin window"
                }
                StatChip(v, l, if (isOverdue) StatusAmber else IrishOrange, alignEnd = true)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = DividerGreen, thickness = 1.dp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📦", fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = courierNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            if (isOverdue) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusAmber.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Your application may be overdue — email the Visa Office for a status update.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusAmber,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(value: String, label: String, color: Color, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = color,
            letterSpacing = (-0.5).sp,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            lineHeight = 14.sp,
        )
    }
}

// ── Accent card ───────────────────────────────────────────────────────────────
@Composable
internal fun AccentCard(
    title: String,
    accentColor: Color = IrishGreen,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth().animateContentSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Row(modifier = Modifier.heightIn(min = 64.dp)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.35f))),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = accentColor,
                    letterSpacing = 0.8.sp,
                )
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}

// ── How to use sheet ─────────────────────────────────────────────────────────
@Composable
private fun HowToUseSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ℹ️", fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                "How to Use",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = IrishGreenDark,
            )
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGreen)
        Spacer(Modifier.height(16.dp))

        HowToStep("🌍", "Select your embassy", "Choose the Irish Embassy that processes applications for your country.")
        HowToStep("📅", "Enter submission date", "Pick the date you lodged your documents at the VAC.")
        HowToStep("📍", "Choose VAC location", "Select your city — transit days to the embassy vary by location.")
        HowToStep("🛂", "Select visa type", "Short Stay (C), Study (D), or Join Family (D).")
        HowToStep("🗓", "View decision window", "See your earliest and latest expected dates, with weekends and public holidays factored in.")
        HowToStep("🔔", "Record your outcome", "Once you hear back, tap Granted, Refused, or No Decision Yet.")

        Spacer(Modifier.height(20.dp))
        TextButton(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
        ) { Text("Close", fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun HowToStep(emoji: String, title: String, desc: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 14.sp)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

// ── No Decision sheet ─────────────────────────────────────────────────────────
@Composable
private fun NoDecisionSheet(onClose: () -> Unit, onOpenDecisions: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🕐", fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text = "No Decision Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = IrishGreenDark,
            )
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGreen)
        Spacer(Modifier.height(14.dp))
        Text(
            text = "The Embassy publishes a daily decisions list. Check whether your name appears:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick   = onOpenDecisions,
            modifier  = Modifier.fillMaxWidth().height(52.dp),
            shape     = RoundedCornerShape(12.dp),
            colors    = ButtonDefaults.buttonColors(containerColor = IrishGreen, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        ) {
            Text("🌐  Open Visa Decisions Page", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(10.dp))
        TextButton(
            onClick  = onClose,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.textButtonColors(contentColor = TextSecondary),
        ) { Text("Close", fontWeight = FontWeight.SemiBold) }
    }
}
