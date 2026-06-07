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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.*
import com.shayshankrathore.irishvisadate.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FMT_TP = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private fun LocalDate.fmtTp() = format(DATE_FMT_TP)

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlannerScreen(onBack: () -> Unit) {

    var travelDate       by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEmbassy  by remember { mutableStateOf(ALL_EMBASSIES[0]) }
    var selectedVac      by remember { mutableStateOf(ALL_EMBASSIES[0].vacOptions[0]) }
    var selectedVisa     by remember { mutableStateOf(VisaType.SHORT_STAY) }
    var embassyExpanded  by remember { mutableStateOf(false) }
    var showDatePicker   by remember { mutableStateOf(false) }

    val todayMs = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis > todayMs
        },
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        travelDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Travel Date Planner", fontWeight = FontWeight.ExtraBold) },
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
            AccentCard(title = "✈️  WHEN DO YOU WANT TO TRAVEL?", accentColor = IrishGreen) {
                Text(
                    "Enter your target travel date and we'll calculate the latest safe submission deadline — working backwards from when your passport needs to be back in your hands.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(10.dp))
                if (travelDate != null) {
                    Text(
                        travelDate!!.fmtTp(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = IrishGreenDark,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape  = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, IrishGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                ) {
                    Text(
                        if (travelDate == null) "Pick travel date" else "Change date",
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            AccentCard(title = "🌍  EMBASSY & VISA", accentColor = IrishGreen) {
                ExposedDropdownMenuBox(
                    expanded = embassyExpanded,
                    onExpandedChange = { embassyExpanded = it },
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
                        ),
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Embassy") },
                    )
                    ExposedDropdownMenu(
                        expanded = embassyExpanded,
                        onDismissRequest = { embassyExpanded = false },
                    ) {
                        ALL_EMBASSIES.forEach { e ->
                            DropdownMenuItem(
                                text = { Text("${e.flag}  ${e.label}") },
                                onClick = {
                                    selectedEmbassy = e
                                    selectedVac     = e.vacOptions[0]
                                    embassyExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("VAC LOCATION", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = TextHint, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedEmbassy.vacOptions.forEach { vac ->
                        val sel = vac == selectedVac
                        Surface(
                            onClick = { selectedVac = vac },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) IrishGreen else DividerGreen),
                            color  = if (sel) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = sel, onClick = { selectedVac = vac },
                                    colors = RadioButtonDefaults.colors(selectedColor = IrishGreen))
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(vac.label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, color = if (sel) IrishGreenDark else TextSecondary)
                                    Text("${vac.cities} — ${vac.transitDays}d transit", style = MaterialTheme.typography.labelSmall, color = TextHint)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("VISA TYPE", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = TextHint, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    VisaType.entries.forEach { vt ->
                        val sel = vt == selectedVisa
                        Surface(
                            onClick = { selectedVisa = vt },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (sel) IrishGreen else DividerGreen),
                            color  = if (sel) IrishGreen.copy(alpha = 0.08f) else SurfaceCard,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = sel, onClick = { selectedVisa = vt },
                                    colors = RadioButtonDefaults.colors(selectedColor = IrishGreen))
                                Spacer(Modifier.width(6.dp))
                                Text(vt.label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, color = if (sel) IrishGreenDark else TextSecondary, modifier = Modifier.weight(1f))
                                Text("€${vt.feeEur}", style = MaterialTheme.typography.labelSmall, color = if (sel) IrishGreen else TextHint)
                            }
                        }
                    }
                }
            }

            if (travelDate != null) {
                val holidays = selectedEmbassy.holidays
                val courierDays = selectedEmbassy.courierDays

                val needDecisionBy    = travelDate!!.subtractWorkingDays(courierDays, holidays)
                val receiveForLatest  = needDecisionBy.subtractWorkingDays(selectedVisa.maxDays, holidays)
                val latestSubmit      = receiveForLatest.subtractWorkingDays(selectedVac.transitDays, holidays)

                val receiveForRecom   = needDecisionBy.subtractWorkingDays(selectedVisa.minDays, holidays)
                val recommendedSubmit = receiveForRecom.subtractWorkingDays(selectedVac.transitDays, holidays)
                    .subtractWorkingDays(10, holidays)

                val today       = LocalDate.now()
                val isLateAlert = latestSubmit.isBefore(today.plusDays(7))
                val isTooLate   = latestSubmit.isBefore(today)

                AccentCard(
                    title = "📋  SUBMISSION DEADLINES",
                    accentColor = if (isTooLate) StatusRed else if (isLateAlert) StatusAmber else IrishGreen,
                ) {
                    if (isTooLate) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StatusRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                        ) {
                            Text(
                                "⚠️ The latest submission deadline has already passed for this travel date. Consider moving your travel date later or applying immediately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusRed,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    } else if (isLateAlert) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(StatusAmber.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                        ) {
                            Text(
                                "⚠️ Deadline is very soon. Apply immediately to have any chance.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusAmber,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }

                    DeadlineRow(
                        emoji   = "🟡",
                        label   = "Latest possible submission",
                        date    = latestSubmit,
                        note    = "Decision arrives on last day of window — cutting it fine",
                        color   = if (isTooLate) StatusRed else StatusAmber,
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = DividerGreen)
                    Spacer(Modifier.height(12.dp))
                    DeadlineRow(
                        emoji = "🟢",
                        label = "Recommended submission",
                        date  = recommendedSubmit,
                        note  = "10-day buffer beyond worst-case decision. Apply by this date.",
                        color = IrishGreen,
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = DividerGreen)
                    Spacer(Modifier.height(10.dp))
                    Text("HOW THIS IS CALCULATED", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = TextHint, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(6.dp))
                    CalcRow("Travel date", travelDate!!.fmtTp())
                    CalcRow("− ${courierDays}d courier return", "→ Decision needed by ${needDecisionBy.fmtTp()}")
                    CalcRow("− ${selectedVisa.maxDays}d max processing", "→ VAC must send by ${receiveForLatest.fmtTp()}")
                    CalcRow("− ${selectedVac.transitDays}d VAC transit", "→ Latest submit: ${latestSubmit.fmtTp()}")
                }

                AccentCard(title = "📊  ALL VAC OPTIONS COMPARED", accentColor = IrishGreen) {
                    Text(
                        "Latest safe submission deadline by VAC location for ${travelDate!!.fmtTp()}:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(10.dp))
                    selectedEmbassy.vacOptions.forEach { vac ->
                        val recvForLate  = needDecisionBy.subtractWorkingDays(selectedVisa.maxDays, holidays)
                        val lateSubmit   = recvForLate.subtractWorkingDays(vac.transitDays, holidays)
                        val isPast       = lateSubmit.isBefore(today)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(if (isPast) "🔴" else "🟢", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(vac.label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IrishGreenDark)
                                Text(vac.cities, style = MaterialTheme.typography.labelSmall, color = TextHint)
                            }
                            Text(
                                if (isPast) "Passed" else lateSubmit.fmtTp(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isPast) StatusRed else IrishGreen,
                            )
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
private fun DeadlineRow(emoji: String, label: String, date: LocalDate, note: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text(
                date.fmtTp(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = color,
            )
            Text(note, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun CalcRow(step: String, result: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(step, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(result, style = MaterialTheme.typography.labelSmall, color = IrishGreenDark, fontWeight = FontWeight.SemiBold)
    }
}
