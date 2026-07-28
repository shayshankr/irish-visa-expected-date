package com.shayshankrathore.irishvisadate.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.shayshankrathore.irishvisadate.ALL_EMBASSIES
import com.shayshankrathore.irishvisadate.AppPreferences
import com.shayshankrathore.irishvisadate.AppPreferences.SavedApplication
import com.shayshankrathore.irishvisadate.Embassy
import com.shayshankrathore.irishvisadate.PdfExportHelper
import com.shayshankrathore.irishvisadate.subtractWorkingDays
import com.shayshankrathore.irishvisadate.JOIN_FAMILY_MAX_DAYS
import com.shayshankrathore.irishvisadate.JOIN_FAMILY_MIN_DAYS
import com.shayshankrathore.irishvisadate.NotificationWorker
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

// ── Domain types ──────────────────────────────────────────────────────────────

enum class VisaType(val label: String, val minDays: Int, val maxDays: Int, val feeEur: Int) {
    SHORT_STAY("Short Stay C",             SHORT_STAY_MIN_DAYS,     SHORT_STAY_MAX_DAYS,     60),
    STUDY("Study (D)",                     STUDY_MIN_DAYS,          STUDY_MAX_DAYS,          100),
    JOIN_FAMILY("Join Family (D)",         JOIN_FAMILY_MIN_DAYS,    JOIN_FAMILY_MAX_DAYS,    100),
    WORK_PERMIT("Critical Skills / Work (D)", WORK_PERMIT_MIN_DAYS, WORK_PERMIT_MAX_DAYS,   100),
    WORKING_HOLIDAY("Working Holiday (D)", WORKING_HOLIDAY_MIN_DAYS, WORKING_HOLIDAY_MAX_DAYS, 100),
    TRANSIT("Transit (A)",                 TRANSIT_MIN_DAYS,        TRANSIT_MAX_DAYS,        60),
}

private val DATE_FMT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

private fun LocalDate.fmt(): String = format(DATE_FMT)
private fun plural(n: Long) = if (n == 1L) "" else "s"

enum class AppScreen {
    TRACKER, GRANTED, REFUSED, CHECKLIST, DECISIONS_WEB, APP_LIST,
    FAQ, NOTIFICATION_SETTINGS, BACKUP,
    TRAVEL_PLANNER, COMMUNITY_TIMES, DOC_TRACKER, COST_CALCULATOR, VISA_VALIDITY,
    LANGUAGE_SETTINGS, APPLICATION_STATUS,
}

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
        val t  = i / 7f
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
                        color = Color.White, fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp, letterSpacing = (-0.3).sp,
                    )
                    Text(
                        text = "Embassy Decision Calculator  🇮🇪",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 12.sp, letterSpacing = 0.2.sp,
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

    var selectedEmbassy      by remember { mutableStateOf(ALL_EMBASSIES[0]) }
    var selectedVac          by remember { mutableStateOf(ALL_EMBASSIES[0].vacOptions[0]) }
    var submissionDate       by remember { mutableStateOf<LocalDate?>(null) }
    var visaType             by remember { mutableStateOf(VisaType.SHORT_STAY) }
    var passportExpiry       by remember { mutableStateOf<LocalDate?>(null) }
    var biometricDate        by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker       by remember { mutableStateOf(false) }
    var showPassportPicker   by remember { mutableStateOf(false) }
    var showBiometricPicker  by remember { mutableStateOf(false) }
    var embassyExpanded      by remember { mutableStateOf(false) }
    var showHowToUse         by remember { mutableStateOf(false) }
    var showNoDecision       by remember { mutableStateOf(false) }
    var showContactSheet     by remember { mutableStateOf(false) }
    var showCompareVac       by remember { mutableStateOf(false) }
    var stateRestored        by remember { mutableStateOf(false) }

    val savedState   by AppPreferences.flow(context).collectAsState(initial = null)
    val notifSettings by AppPreferences.notificationSettingsFlow(context)
        .collectAsState(initial = AppPreferences.NotificationSettings())

    // Restore persisted state once on first load
    LaunchedEffect(savedState) {
        if (!stateRestored && savedState != null) {
            savedState!!.embassyId?.let { id ->
                ALL_EMBASSIES.find { it.id == id }?.also { e ->
                    selectedEmbassy = e
                    savedState!!.vacLabel?.let { l ->
                        e.vacOptions.find { it.label == l }?.let { selectedVac = it }
                    }
                }
            }
            savedState!!.submissionDate?.let { s ->
                runCatching { LocalDate.parse(s) }.getOrNull()?.let { submissionDate = it }
            }
            savedState!!.visaTypeName?.let { n ->
                runCatching { VisaType.valueOf(n) }.getOrNull()?.let { visaType = it }
            }
            savedState!!.passportExpiry?.let { s ->
                runCatching { LocalDate.parse(s) }.getOrNull()?.let { passportExpiry = it }
            }
            savedState!!.biometricDate?.let { s ->
                runCatching { LocalDate.parse(s) }.getOrNull()?.let { biometricDate = it }
            }
            stateRestored = true
        }
    }

    // Persist state whenever user changes anything
    LaunchedEffect(selectedEmbassy, selectedVac, submissionDate, visaType, passportExpiry, biometricDate) {
        if (stateRestored) {
            AppPreferences.save(
                context        = context,
                embassyId      = selectedEmbassy.id,
                vacLabel       = selectedVac.label,
                submissionDate = submissionDate?.toString(),
                visaTypeName   = visaType.name,
                passportExpiry = passportExpiry?.toString(),
                biometricDate  = biometricDate?.toString(),
            )
        }
    }

    // Schedule / reschedule notifications
    LaunchedEffect(selectedEmbassy, selectedVac, submissionDate, visaType, notifSettings) {
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
        if (notifSettings.windowOpens) schedule(earliest.minusDays(1), NotificationWorker.TYPE_WINDOW_OPENS)
        if (notifSettings.midpoint && winDays > 2) schedule(earliest.plusDays(winDays / 2), NotificationWorker.TYPE_MIDPOINT)
        if (notifSettings.overdue) schedule(latest.plusDays(1), NotificationWorker.TYPE_OVERDUE)

        // Decision watchdog — periodic check of decisions page
        val watchdogTag = "visa_watchdog_${selectedEmbassy.id}"
        wm.cancelAllWorkByTag(watchdogTag)
        if (notifSettings.watchdog && selectedEmbassy.decisionsUrl.isNotBlank()) {
            wm.enqueue(
                androidx.work.PeriodicWorkRequestBuilder<com.shayshankrathore.irishvisadate.DecisionWatchdogWorker>(
                    4, java.util.concurrent.TimeUnit.HOURS
                )
                .setInputData(androidx.work.workDataOf(
                    com.shayshankrathore.irishvisadate.DecisionWatchdogWorker.KEY_URL        to selectedEmbassy.decisionsUrl,
                    com.shayshankrathore.irishvisadate.DecisionWatchdogWorker.KEY_EMBASSY_ID to selectedEmbassy.id,
                    com.shayshankrathore.irishvisadate.DecisionWatchdogWorker.KEY_LABEL      to selectedEmbassy.label,
                ))
                .addTag(watchdogTag)
                .build()
            )
        }
    }

    val scope                = rememberCoroutineScope()
    val sheetState           = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val howToUseSheetState   = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val contactSheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val compareVacSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Date pickers
    val submissionPickerState = rememberDatePickerState(
        initialSelectedDateMillis = submissionDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        },
    )
    val passportPickerState = rememberDatePickerState()
    val biometricPickerState = rememberDatePickerState(
        initialSelectedDateMillis = biometricDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    submissionPickerState.selectedDateMillis?.let {
                        submissionDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            },
        ) { DatePicker(state = submissionPickerState) }
    }

    if (showPassportPicker) {
        DatePickerDialog(
            onDismissRequest = { showPassportPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    passportPickerState.selectedDateMillis?.let {
                        passportExpiry = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showPassportPicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPassportPicker = false }) { Text("Cancel", color = TextSecondary) }
            },
        ) { DatePicker(state = passportPickerState) }
    }

    if (showBiometricPicker) {
        DatePickerDialog(
            onDismissRequest = { showBiometricPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    biometricPickerState.selectedDateMillis?.let {
                        biometricDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showBiometricPicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showBiometricPicker = false }) { Text("Cancel", color = TextSecondary) }
            },
        ) { DatePicker(state = biometricPickerState) }
    }

    if (showNoDecision) {
        ModalBottomSheet(
            onDismissRequest = { showNoDecision = false },
            sheetState = sheetState, containerColor = SurfaceCard,
            dragHandle = { SheetDragHandle() }
        ) {
            NoDecisionSheet(
                onClose        = { scope.launch { sheetState.hide() }.invokeOnCompletion { showNoDecision = false } },
                onOpenDecisions = { onOpenDecisions(selectedEmbassy.decisionsUrl) },
            )
        }
    }

    if (showHowToUse) {
        ModalBottomSheet(
            onDismissRequest = { showHowToUse = false },
            sheetState = howToUseSheetState, containerColor = SurfaceCard,
            dragHandle = { SheetDragHandle() }
        ) {
            HowToUseSheet(onClose = {
                scope.launch { howToUseSheetState.hide() }.invokeOnCompletion { showHowToUse = false }
            })
        }
    }

    if (showContactSheet) {
        ModalBottomSheet(
            onDismissRequest = { showContactSheet = false },
            sheetState = contactSheetState, containerColor = SurfaceCard,
            dragHandle = { SheetDragHandle() }
        ) {
            EmbassyContactSheet(
                embassy = selectedEmbassy,
                onClose = { scope.launch { contactSheetState.hide() }.invokeOnCompletion { showContactSheet = false } },
            )
        }
    }

    if (showCompareVac && submissionDate != null) {
        ModalBottomSheet(
            onDismissRequest = { showCompareVac = false },
            sheetState = compareVacSheetState, containerColor = SurfaceCard,
            dragHandle = { SheetDragHandle() }
        ) {
            CompareVacSheet(
                embassy        = selectedEmbassy,
                submissionDate = submissionDate!!,
                visaType       = visaType,
                onClose        = { scope.launch { compareVacSheetState.hide() }.invokeOnCompletion { showCompareVac = false } },
            )
        }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = { IrishHeader(onHelpClick = { showHowToUse = true }) },
    ) { inner ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(inner)) {
            val isWide = maxWidth >= 600.dp

            if (isWide) {
                // ── Tablet: centred, max width ────────────────────────────────
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 580.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        ScreenContent(
                            context, today, todayMs, scope,
                            selectedEmbassy, selectedVac, submissionDate, visaType,
                            passportExpiry, biometricDate,
                            embassyExpanded,
                            onEmbassyExpandedChange = { embassyExpanded = it },
                            onEmbassySelected = { e -> selectedEmbassy = e; selectedVac = e.vacOptions[0]; embassyExpanded = false },
                            onVacSelected  = { selectedVac = it },
                            onVisaSelected = { visaType = it },
                            onPickDate     = { showDatePicker = true },
                            onPickPassport = { showPassportPicker = true },
                            onPickBiometric = { showBiometricPicker = true },
                            onClearPassport = { passportExpiry = null },
                            onClearBiometric = { biometricDate = null },
                            onNavigate, onOpenChecklist,
                            onShowContact    = { showContactSheet = true },
                            onShowNoDecision = { showNoDecision = true },
                            onShowCompareVac = { showCompareVac = true },
                        )
                    }
                }
            } else {
                // ── Phone: single column ──────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ScreenContent(
                        context, today, todayMs, scope,
                        selectedEmbassy, selectedVac, submissionDate, visaType,
                        passportExpiry, biometricDate,
                        embassyExpanded,
                        onEmbassyExpandedChange = { embassyExpanded = it },
                        onEmbassySelected = { e -> selectedEmbassy = e; selectedVac = e.vacOptions[0]; embassyExpanded = false },
                        onVacSelected  = { selectedVac = it },
                        onVisaSelected = { visaType = it },
                        onPickDate     = { showDatePicker = true },
                        onPickPassport = { showPassportPicker = true },
                        onPickBiometric = { showBiometricPicker = true },
                        onClearPassport = { passportExpiry = null },
                        onClearBiometric = { biometricDate = null },
                        onNavigate, onOpenChecklist,
                        onShowContact    = { showContactSheet = true },
                        onShowNoDecision = { showNoDecision = true },
                        onShowCompareVac = { showCompareVac = true },
                    )
                }
            }
        }
    }
}

// ── Screen content (shared between phone and tablet) ─────────────────────────
@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenContent(
    context: android.content.Context,
    today: LocalDate,
    todayMs: Long,
    scope: CoroutineScope,
    selectedEmbassy: Embassy,
    selectedVac: VacOption,
    submissionDate: LocalDate?,
    visaType: VisaType,
    passportExpiry: LocalDate?,
    biometricDate: LocalDate?,
    embassyExpanded: Boolean,
    onEmbassyExpandedChange: (Boolean) -> Unit,
    onEmbassySelected: (Embassy) -> Unit,
    onVacSelected: (VacOption) -> Unit,
    onVisaSelected: (VisaType) -> Unit,
    onPickDate: () -> Unit,
    onPickPassport: () -> Unit,
    onPickBiometric: () -> Unit,
    onClearPassport: () -> Unit,
    onClearBiometric: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onOpenChecklist: (VisaType) -> Unit,
    onShowContact: () -> Unit,
    onShowNoDecision: () -> Unit,
    onShowCompareVac: () -> Unit,
) {
    // ── Embassy selector ──────────────────────────────────────────────────────
    AccentCard(title = "🌍  IRISH EMBASSY", accentColor = IrishGreen) {
        ExposedDropdownMenuBox(
            expanded = embassyExpanded,
            onExpandedChange = onEmbassyExpandedChange,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = "${selectedEmbassy.flag}  ${selectedEmbassy.label}",
                onValueChange = {}, readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = embassyExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = IrishGreen,
                    unfocusedBorderColor = IrishGreen.copy(alpha = 0.45f),
                    focusedTextColor     = IrishGreenDark,
                    unfocusedTextColor   = IrishGreenDark,
                    focusedTrailingIconColor   = IrishGreen,
                    unfocusedTrailingIconColor = IrishGreenDark,
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
            )
            ExposedDropdownMenu(expanded = embassyExpanded, onDismissRequest = { onEmbassyExpandedChange(false) }) {
                ALL_EMBASSIES.forEach { embassy ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "${embassy.flag}  ${embassy.label}",
                                fontWeight = if (embassy == selectedEmbassy) FontWeight.Bold else FontWeight.Normal,
                                color      = if (embassy == selectedEmbassy) IrishGreenDark else TextSecondary,
                            )
                        },
                        onClick = { onEmbassySelected(embassy) },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        // Visa fee note
        Spacer(Modifier.height(6.dp))
        Text(
            "Application fee: €${visaType.feeEur} (non-refundable)",
            style = MaterialTheme.typography.labelSmall,
            color = TextHint,
        )
        if (selectedEmbassy.vacBookingUrl.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { openCustomTab(context, selectedEmbassy.vacBookingUrl) },
                shape  = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
            ) {
                Text("🌐 Book VAC appointment", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }

    // ── Submission date ───────────────────────────────────────────────────────
    AccentCard(title = "📅  APPLICATION SUBMITTED", accentColor = IrishGreen) {
        if (submissionDate != null) {
            Text(
                text = submissionDate.fmt(),
                fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                color = IrishGreenDark, letterSpacing = (-0.3).sp,
            )
            Spacer(Modifier.height(8.dp))
        }
        OutlinedButton(
            onClick = onPickDate,
            shape  = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
        ) {
            Text(if (submissionDate == null) "Pick submission date" else "Change date", fontWeight = FontWeight.SemiBold)
        }
    }

    // ── Biometric appointment (optional) ─────────────────────────────────────
    AccentCard(title = "🫆  BIOMETRIC APPOINTMENT (OPTIONAL)", accentColor = IrishGreen) {
        Text(
            "Some VACs require a biometric appointment before formal file submission.",
            style = MaterialTheme.typography.bodySmall, color = TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        if (biometricDate != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    biometricDate.fmt(),
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IrishGreenDark,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearBiometric) { Text("✕ Clear", color = TextSecondary, fontSize = 12.sp) }
            }
            Spacer(Modifier.height(4.dp))
        }
        OutlinedButton(
            onClick = onPickBiometric,
            shape  = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
        ) {
            Text(if (biometricDate == null) "Set biometric date" else "Change date", fontWeight = FontWeight.SemiBold)
        }
    }

    // ── VAC location ─────────────────────────────────────────────────────────
    AccentCard(title = "📍  VISA APPLICATION CENTRE (VAC) LOCATION", accentColor = IrishGreen) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            selectedEmbassy.vacOptions.forEach { vac ->
                val selected = selectedVac == vac
                Surface(
                    onClick = { onVacSelected(vac) },
                    shape   = RoundedCornerShape(10.dp),
                    border  = androidx.compose.foundation.BorderStroke(1.5.dp, if (selected) IrishGreen else DividerGreen),
                    color   = if (selected) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected, onClick = { onVacSelected(vac) },
                            colors = RadioButtonDefaults.colors(selectedColor = IrishGreen),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                vac.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = if (selected) IrishGreenDark else TextSecondary,
                            )
                            Text(
                                "${vac.cities} — ${vac.transitDays} working day${plural(vac.transitDays.toLong())} transit",
                                style = MaterialTheme.typography.labelSmall, color = TextHint,
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Visa type ─────────────────────────────────────────────────────────────
    AccentCard(title = "🛂  VISA TYPE", accentColor = IrishGreen) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisaType.entries.forEach { type ->
                val selected = visaType == type
                Surface(
                    onClick = { onVisaSelected(type) },
                    shape   = RoundedCornerShape(10.dp),
                    border  = androidx.compose.foundation.BorderStroke(1.5.dp, if (selected) IrishGreen else DividerGreen),
                    color   = if (selected) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected, onClick = { onVisaSelected(type) },
                            colors = RadioButtonDefaults.colors(selectedColor = IrishGreen),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            type.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (selected) IrishGreenDark else TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "€${type.feeEur}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) IrishGreen else TextHint,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    // ── Passport expiry (optional) ────────────────────────────────────────────
    AccentCard(title = "🛂  PASSPORT EXPIRY (OPTIONAL)", accentColor = IrishGreen) {
        Text(
            "Add your passport expiry date to check for validity warnings.",
            style = MaterialTheme.typography.bodySmall, color = TextSecondary,
        )
        Spacer(Modifier.height(8.dp))
        if (passportExpiry != null) {
            val expiryWarning: String? = if (submissionDate != null) {
                val holidays = selectedEmbassy.holidays
                val receive  = submissionDate.addWorkingDays(selectedVac.transitDays, holidays)
                val latest   = receive.addWorkingDays(visaType.maxDays, holidays)
                when {
                    passportExpiry.isBefore(today) ->
                        "⚠️ Passport expired on ${passportExpiry.fmt()}. Your application may be invalid."
                    passportExpiry.isBefore(latest.plusMonths(6)) ->
                        "⚠️ Passport expires ${passportExpiry.fmt()} — ensure it remains valid throughout your stay in Ireland."
                    else -> null
                }
            } else null

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    passportExpiry.fmt(),
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IrishGreenDark,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onClearPassport) { Text("✕ Clear", color = TextSecondary, fontSize = 12.sp) }
            }
            if (expiryWarning != null) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusAmber.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                ) {
                    Text(expiryWarning, style = MaterialTheme.typography.bodySmall, color = StatusAmber)
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        OutlinedButton(
            onClick = onPickPassport,
            shape  = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
        ) {
            Text(if (passportExpiry == null) "Set passport expiry" else "Change date", fontWeight = FontWeight.SemiBold)
        }
    }

    // ── Decision window ───────────────────────────────────────────────────────
    if (submissionDate != null) {
        val holidays          = selectedEmbassy.holidays
        val embassyReceive    = submissionDate.addWorkingDays(selectedVac.transitDays, holidays)
        val earliest          = embassyReceive.addWorkingDays(visaType.minDays, holidays)
        val latest            = embassyReceive.addWorkingDays(visaType.maxDays, holidays)
        val passportReturn    = latest.addWorkingDays(selectedEmbassy.courierDays, holidays)
        val windowDays        = workingDaysBetween(earliest, latest, holidays)
        val elapsed           = workingDaysBetween(earliest, today, holidays).coerceAtLeast(0L)
        val elapsedSub        = workingDaysBetween(submissionDate, today, holidays).coerceAtLeast(0L)
        val isBeforeWindow    = today.isBefore(earliest)
        val isOverdue         = today.isAfter(latest)
        val isInWindow        = !isBeforeWindow && !isOverdue
        val progress          = when {
            isBeforeWindow -> 0f
            isOverdue      -> 1f
            windowDays > 0 -> (elapsed.toFloat() / windowDays).coerceIn(0f, 1f)
            else           -> 1f
        }

        val emailBody = buildString {
            append("Dear Visa Officer,\n\n")
            append("I am writing to enquire about the status of my Irish visa application.\n\n")
            append("Visa Type: ${visaType.label}\n")
            append("Embassy: ${selectedEmbassy.label}\n")
            append("VAC Location: ${selectedVac.label}\n")
            append("Submission Date: ${submissionDate.fmt()}\n\n")
            append("The expected processing window passed on ${latest.fmt()}, and I have not yet received a decision. ")
            append("Could you please advise on the current status of my application?\n\n")
            append("Yours sincerely,\n[Your Full Name]\n[Your Passport Number]\n[Your Reference Number]")
        }
        val onEmailVisa = {
            context.startActivity(
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${selectedEmbassy.contact.email}")
                    putExtra(Intent.EXTRA_SUBJECT, "Irish Visa Application Status Enquiry")
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                }
            )
        }

        DecisionWindowCard(
            today = today, earliest = earliest, latest = latest,
            elapsedSub = elapsedSub, windowProgress = progress,
            isBeforeWindow = isBeforeWindow, isOverdue = isOverdue, isInWindow = isInWindow,
            transit = selectedVac.transitDays,
            courierNote = selectedEmbassy.courierNote,
            passportReturnDate = passportReturn, courierDays = selectedEmbassy.courierDays,
            onEmailVisa = if (isOverdue) onEmailVisa else null,
        )

        // ── Seasonal warnings ─────────────────────────────────────────────────
        val seasonalWarnings = getSeasonalWarnings(earliest, latest)
        if (seasonalWarnings.isNotEmpty()) {
            AccentCard(title = "🐢  SEASONAL SLOWDOWN WARNING", accentColor = StatusAmber) {
                seasonalWarnings.forEach { msg ->
                    Text(msg, style = MaterialTheme.typography.bodySmall, color = StatusAmber, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }

        // ── Quick actions ─────────────────────────────────────────────────────
        AccentCard(title = "⚡  QUICK ACTIONS", accentColor = IrishGreen) {
            val shareText = buildString {
                append("🇮🇪 Irish Visa Application\n\n")
                append("Type: ${visaType.label}\n")
                append("Embassy: ${selectedEmbassy.label}\n")
                append("Submitted: ${submissionDate.fmt()}\n\n")
                append("Decision window: ${earliest.fmt()} – ${latest.fmt()}\n")
                append("Passport back by: ~${passportReturn.fmt()}\n\n")
                append("via Irish Visa Tracker")
            }
            // Row 1: Share, Calendar, PDF
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickButton("📤 Share", Modifier.weight(1f), smallText = true) {
                    context.startActivity(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, shareText) },
                            "Share"
                        )
                    )
                }
                QuickButton("📅 Calendar", Modifier.weight(1f), smallText = true) {
                    context.startActivity(
                        Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "Irish Visa Decision Window")
                            putExtra(CalendarContract.Events.DESCRIPTION, "Visa: ${visaType.label}\nEmbassy: ${selectedEmbassy.label}\nExpected: ${earliest.fmt()} – ${latest.fmt()}")
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, earliest.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, latest.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
                            putExtra(CalendarContract.Events.ALL_DAY, true)
                        }
                    )
                }
                QuickButton("📄 PDF", Modifier.weight(1f), smallText = true) {
                    PdfExportHelper.export(context, visaType, selectedEmbassy, selectedVac,
                        submissionDate, earliest, latest, passportReturn)
                }
            }
            Spacer(Modifier.height(6.dp))
            // Row 2: Checklist, My Apps, Check Status
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickButton("📋 Checklist", Modifier.weight(1f), smallText = true) { onOpenChecklist(visaType) }
                QuickButton("📁 My Apps",   Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.APP_LIST) }
                QuickButton("🔎 Check",    Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.APPLICATION_STATUS) }
            }
            Spacer(Modifier.height(6.dp))
            // Row 3: Contact, Notifications, Backup
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickButton("📞 Contact",   Modifier.weight(1f), smallText = true) { onShowContact() }
                QuickButton("🔔 Notifs",    Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.NOTIFICATION_SETTINGS) }
                QuickButton("🗃 Backup",    Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.BACKUP) }
            }
            Spacer(Modifier.height(6.dp))
            // Row 3b: Language Settings
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickButton("🌐 Language",  Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.LANGUAGE_SETTINGS) }
                Spacer(Modifier.weight(2f))
            }
            Spacer(Modifier.height(6.dp))
            // Row 4: Travel Planner, Visa Validity, Costs
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickButton("✈️ Travel Plan", Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.TRAVEL_PLANNER) }
                QuickButton("📋 Validity",   Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.VISA_VALIDITY) }
                QuickButton("💶 Costs",      Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.COST_CALCULATOR) }
            }
            Spacer(Modifier.height(6.dp))
            // Row 5: Docs, Community, Compare VACs
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QuickButton("📂 Docs",      Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.DOC_TRACKER) }
                QuickButton("📊 Times",     Modifier.weight(1f), smallText = true) { onNavigate(AppScreen.COMMUNITY_TIMES) }
                QuickButton("⚖️ Compare",   Modifier.weight(1f), smallText = true) { onShowCompareVac() }
            }
            Spacer(Modifier.height(10.dp))
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
                                submissionDate = submissionDate.toString(),
                                visaTypeName   = visaType.name,
                                visaTypeLabel  = visaType.label,
                                savedAt        = LocalDate.now().toString(),
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = IrishGreen, contentColor = Color.White),
            ) {
                Text("💾  Save Application", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // ── Status update ─────────────────────────────────────────────────────
        AccentCard(title = "🔔  GOT AN UPDATE?", accentColor = IrishOrange) {
            Text(
                "Let us know the outcome of your application:",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick   = { onNavigate(AppScreen.GRANTED) },
                    modifier  = Modifier.fillMaxWidth().height(52.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = IrishGreen, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) { Text("✅  I got my visa!", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

                Button(
                    onClick  = { onNavigate(AppScreen.REFUSED) },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                        .border(1.5.dp, StatusRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedLight, contentColor = StatusRed),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) { Text("❌  My visa was refused", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }

                OutlinedButton(
                    onClick  = onShowNoDecision,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen.copy(alpha = 0.6f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreenDark),
                ) { Text("🕐  No decision yet", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}

// ── Quick action button helper ────────────────────────────────────────────────
@Composable
private fun QuickButton(label: String, modifier: Modifier, smallText: Boolean = false, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape  = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = if (smallText) 11.sp else 13.sp, maxLines = 1)
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
    onEmailVisa: (() -> Unit)?,
) {
    val bgGradient  = when {
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
                .width(5.dp).fillMaxHeight()
                .background(Brush.verticalGradient(listOf(IrishGreen, IrishOrange)), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        )
        Column(modifier = Modifier.padding(start = 18.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🗓", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text("EXPECTED DECISION WINDOW", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = IrishGreenDark, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text("Expect a decision between", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text("${earliest.fmt()}  —  ${latest.fmt()}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = dateColor, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(4.dp))
            Text("(includes $transit working day${plural(transit.toLong())} VAC transit)", style = MaterialTheme.typography.labelSmall, color = TextHint)

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = DividerGreen, thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📬", fontSize = 15.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Passport back by ~${passportReturnDate.fmt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IrishGreenDark)
                    Text("(includes $courierDays working day${plural(courierDays.toLong())} courier return)", style = MaterialTheme.typography.labelSmall, color = TextHint)
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(DividerGreen)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(windowProgress).fillMaxHeight()
                        .background(Brush.horizontalGradient(listOf(IrishGreen, if (isOverdue) StatusAmber else IrishOrange)), RoundedCornerShape(5.dp))
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
                Text(courierNote, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }

            if (isOverdue) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(StatusAmber.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("⚠️", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Your application may be overdue — email the Visa Office for a status update.",
                        style = MaterialTheme.typography.bodySmall, color = StatusAmber, fontWeight = FontWeight.SemiBold,
                    )
                }
                if (onEmailVisa != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onEmailVisa,
                        modifier = Modifier.fillMaxWidth(),
                        shape  = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusAmber),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAmber),
                    ) {
                        Text("📧 Email Visa Office (pre-filled)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(value: String, label: String, color: Color, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = color, letterSpacing = (-0.5).sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start, lineHeight = 14.sp)
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
                    .width(5.dp).fillMaxHeight()
                    .background(Brush.verticalGradient(listOf(accentColor, accentColor.copy(alpha = 0.35f))), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = accentColor, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(10.dp))
                content()
            }
        }
    }
}

// ── Sheet drag handle ─────────────────────────────────────────────────────────
@Composable
private fun SheetDragHandle() {
    Box(modifier = Modifier.padding(vertical = 12.dp).size(width = 40.dp, height = 4.dp)
        .background(DividerGreen, RoundedCornerShape(2.dp)))
}

// ── Seasonal slowdown warnings ────────────────────────────────────────────────
private data class SlowPeriod(val start: LocalDate, val end: LocalDate, val label: String)

private val SLOW_PERIODS = listOf(
    SlowPeriod(LocalDate.of(2026, 12, 20), LocalDate.of(2027, 1, 10), "Christmas / New Year"),
    SlowPeriod(LocalDate.of(2026, 4, 1),  LocalDate.of(2026, 4, 10), "Easter 2026"),
    SlowPeriod(LocalDate.of(2027, 3, 24), LocalDate.of(2027, 4, 2),  "Easter 2027"),
    SlowPeriod(LocalDate.of(2026, 3, 18), LocalDate.of(2026, 3, 23), "Eid al-Fitr 2026 (approx)"),
    SlowPeriod(LocalDate.of(2027, 3, 8),  LocalDate.of(2027, 3, 13), "Eid al-Fitr 2027 (approx)"),
    SlowPeriod(LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 23), "Diwali 2026 (approx)"),
    SlowPeriod(LocalDate.of(2027, 11, 7),  LocalDate.of(2027, 11, 11), "Diwali 2027 (approx)"),
    SlowPeriod(LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 25), "Chinese New Year 2026"),
    SlowPeriod(LocalDate.of(2027, 2, 6),  LocalDate.of(2027, 2, 15), "Chinese New Year 2027"),
)

private fun getSeasonalWarnings(earliest: LocalDate, latest: LocalDate): List<String> {
    val buffer = 5L
    val windowStart = earliest.minusDays(buffer)
    val windowEnd   = latest.plusDays(buffer)
    return SLOW_PERIODS.filter { period ->
        !(period.end.isBefore(windowStart) || period.start.isAfter(windowEnd))
    }.map { "⚠️ Decision window overlaps ${it.label} — Irish Embassy may process slower during this period." }
}

// ── Compare VAC bottom sheet ──────────────────────────────────────────────────
@Composable
private fun CompareVacSheet(
    embassy: Embassy,
    submissionDate: LocalDate,
    visaType: VisaType,
    onClose: () -> Unit,
) {
    val holidays = embassy.holidays
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(embassy.flag, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text("Compare VAC Options", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = IrishGreenDark)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Submitted: ${submissionDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))}  •  ${visaType.label}",
            style = MaterialTheme.typography.labelSmall, color = TextHint,
        )
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGreen)
        Spacer(Modifier.height(14.dp))

        embassy.vacOptions.forEach { vac ->
            val receive  = submissionDate.addWorkingDays(vac.transitDays, holidays)
            val earliest = receive.addWorkingDays(visaType.minDays, holidays)
            val latest   = receive.addWorkingDays(visaType.maxDays, holidays)
            val fmt      = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = IrishGreen.copy(alpha = 0.07f)),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(vac.label, fontWeight = FontWeight.Bold, color = IrishGreenDark, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text("${vac.transitDays}d transit", style = MaterialTheme.typography.labelSmall, color = TextHint)
                    }
                    Text(vac.cities, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${earliest.format(fmt)} – ${latest.format(fmt)} ${latest.year}",
                        fontWeight = FontWeight.ExtraBold, color = IrishGreen, fontSize = 16.sp,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
            Text("Close", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Embassy contact sheet ─────────────────────────────────────────────────────
@Composable
private fun EmbassyContactSheet(embassy: Embassy, onClose: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(embassy.flag, fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Text(embassy.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = IrishGreenDark)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "⚠️ Verify details at ireland.ie before writing. Contact info may change.",
            style = MaterialTheme.typography.labelSmall, color = TextHint,
        )
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGreen)
        Spacer(Modifier.height(14.dp))

        ContactRow(icon = "📞", label = "Phone", value = embassy.contact.phone, onClick = {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${embassy.contact.phone}")))
        })
        Spacer(Modifier.height(10.dp))
        ContactRow(icon = "📧", label = "Email", value = embassy.contact.email, onClick = {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${embassy.contact.email}")))
        })
        Spacer(Modifier.height(10.dp))
        ContactRow(icon = "📍", label = "Address", value = embassy.contact.address, onClick = null)

        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
            Text("Close", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ContactRow(icon: String, label: String, value: String, onClick: (() -> Unit)?) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextHint)
            if (onClick != null) {
                TextButton(onClick = onClick, contentPadding = PaddingValues(0.dp)) {
                    Text(value, color = IrishGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            } else {
                Text(value, color = IrishGreenDark, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            }
        }
    }
}

// ── How to use sheet ──────────────────────────────────────────────────────────
@Composable
private fun HowToUseSheet(onClose: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ℹ️", fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Text("How to Use", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = IrishGreenDark)
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGreen)
        Spacer(Modifier.height(16.dp))
        HowToStep("🌍", "Select your embassy", "Choose the Irish Embassy that processes applications for your country.")
        HowToStep("📅", "Enter submission date", "Pick the date you lodged your documents at the VAC.")
        HowToStep("📍", "Choose VAC location", "Select your city — transit days to the embassy vary by location.")
        HowToStep("🛂", "Select visa type", "Short Stay (C), Study (D), Working Holiday (D), etc.")
        HowToStep("🗓", "View decision window", "See earliest and latest expected dates, with weekends and holidays factored in.")
        HowToStep("🔔", "Record your outcome", "Once you hear back, tap Granted, Refused, or No Decision Yet.")
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
            Text("Close", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HowToStep(emoji: String, title: String, desc: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
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
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🕐", fontSize = 28.sp)
            Spacer(Modifier.width(10.dp))
            Text("No Decision Yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = IrishGreenDark)
        }
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = DividerGreen)
        Spacer(Modifier.height(14.dp))
        Text(
            "The Embassy publishes a daily decisions list. Check whether your name appears:",
            style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onOpenDecisions,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IrishGreen, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(4.dp),
        ) { Text("🌐  Open Visa Decisions Page", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)) {
            Text("Close", fontWeight = FontWeight.SemiBold)
        }
    }
}
