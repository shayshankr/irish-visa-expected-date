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
import com.shayshankrathore.irishvisadate.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val DATE_FMT_VV = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
private fun LocalDate.fmtVv() = format(DATE_FMT_VV)

private enum class EntryType(val label: String) {
    SINGLE("Single entry"),
    DOUBLE("Double entry"),
    MULTIPLE("Multiple entries"),
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaValidityScreen(onBack: () -> Unit) {
    var grantDate         by remember { mutableStateOf<LocalDate?>(null) }
    var validFromDate     by remember { mutableStateOf<LocalDate?>(null) }
    var durationDays      by remember { mutableStateOf("90") }
    var entryType         by remember { mutableStateOf(EntryType.SINGLE) }
    var entryDate         by remember { mutableStateOf<LocalDate?>(null) }

    var showGrantPicker     by remember { mutableStateOf(false) }
    var showValidFromPicker by remember { mutableStateOf(false) }
    var showEntryPicker     by remember { mutableStateOf(false) }

    val grantPickerState     = rememberDatePickerState()
    val validFromPickerState = rememberDatePickerState()
    val entryPickerState     = rememberDatePickerState()

    @Composable
    fun PickerDialog(state: DatePickerState, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onConfirm(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                }) { Text("OK", color = IrishGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

    if (showGrantPicker)     PickerDialog(grantPickerState,     { grantDate = it },     { showGrantPicker = false })
    if (showValidFromPicker) PickerDialog(validFromPickerState, { validFromDate = it },  { showValidFromPicker = false })
    if (showEntryPicker)     PickerDialog(entryPickerState,     { entryDate = it },     { showEntryPicker = false })

    val today = LocalDate.now()

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Visa Validity Calculator", fontWeight = FontWeight.ExtraBold) },
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
            AccentCard(title = "📋  VISA STICKER DETAILS", accentColor = IrishGreen) {
                Text(
                    "Enter the dates printed on your visa sticker to calculate your entry deadline, last day to leave, and how many days you have remaining.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(14.dp))

                DateField(
                    label = "Grant date (date visa was issued)",
                    date  = grantDate,
                    onPick = { showGrantPicker = true },
                )
                Spacer(Modifier.height(10.dp))
                DateField(
                    label = "Valid from (earliest entry date on sticker)",
                    date  = validFromDate,
                    onPick = { showValidFromPicker = true },
                )
                Spacer(Modifier.height(10.dp))

                Text("Duration of stay (days printed on sticker)", style = MaterialTheme.typography.labelSmall, color = TextHint)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { durationDays = it.filter { c -> c.isDigit() }.take(3) },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        suffix = { Text(" days", color = TextHint) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IrishGreen,
                            unfocusedBorderColor = DividerGreen,
                        ),
                        shape = RoundedCornerShape(10.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("(90 = tourist, 180 = long stay)", style = MaterialTheme.typography.labelSmall, color = TextHint)
                }
                Spacer(Modifier.height(10.dp))

                Text("Entry type", style = MaterialTheme.typography.labelSmall, color = TextHint)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EntryType.entries.forEach { et ->
                        val sel = et == entryType
                        FilterChip(
                            selected = sel,
                            onClick  = { entryType = et },
                            label    = { Text(et.label, fontSize = 12.sp) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IrishGreen.copy(alpha = 0.15f),
                                selectedLabelColor     = IrishGreenDark,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled  = true,
                                selected = sel,
                                selectedBorderColor = IrishGreen,
                            ),
                        )
                    }
                }
            }

            if (validFromDate != null) {
                val duration = durationDays.toIntOrNull() ?: 90
                // Visa validity: typically 1 year from grant (max entry deadline)
                val latestEntry   = grantDate?.plusYears(1)?.minusDays(1) ?: validFromDate!!.plusYears(1).minusDays(1)
                // Last day to LEAVE = first entry date + duration - 1
                // If user hasn't entered yet, assume entry today
                val assumedEntry  = entryDate ?: today

                val mustLeaveBy   = assumedEntry.plusDays((duration - 1).toLong())
                val daysUsed      = if (entryDate != null) ChronoUnit.DAYS.between(entryDate, today).coerceAtLeast(0) else 0L
                val daysRemaining = (duration - daysUsed).coerceAtLeast(0)
                val daysToEntry   = if (entryDate == null) ChronoUnit.DAYS.between(today, latestEntry).coerceAtLeast(0) else 0L

                val hasEntered    = entryDate != null
                val isExpiredVisa = today.isAfter(latestEntry)
                val isOverstayed  = hasEntered && today.isAfter(mustLeaveBy)

                AccentCard(
                    title = "📊  VALIDITY RESULTS",
                    accentColor = when {
                        isOverstayed || isExpiredVisa -> StatusRed
                        daysRemaining < 14 -> StatusAmber
                        else -> IrishGreen
                    },
                ) {
                    if (isOverstayed) {
                        AlertBanner("⚠️ Overstay risk", "You may be past your permitted stay. Leave Ireland immediately and consult an immigration lawyer.", StatusRed)
                        Spacer(Modifier.height(10.dp))
                    } else if (isExpiredVisa) {
                        AlertBanner("⚠️ Visa expired", "The visa validity period has passed. You cannot enter Ireland on this visa.", StatusRed)
                        Spacer(Modifier.height(10.dp))
                    }

                    ResultRow("🗓 Valid from",        validFromDate!!.fmtVv())
                    ResultRow("🔚 Latest entry by",   latestEntry.fmtVv(),
                        note = if (isExpiredVisa) "Passed ${ChronoUnit.DAYS.between(latestEntry, today)} days ago" else "${daysToEntry}d remaining to make first entry")
                    HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))

                    // Entry date
                    Text("ACTUAL ENTRY DATE (optional)", fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = TextHint, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entryDate?.fmtVv() ?: "Not entered yet",
                            fontWeight = FontWeight.SemiBold,
                            color = if (entryDate == null) TextHint else IrishGreenDark,
                            modifier = Modifier.weight(1f),
                        )
                        if (entryDate != null) {
                            TextButton(onClick = { entryDate = null }) { Text("✕ Clear", color = TextSecondary, fontSize = 12.sp) }
                        }
                        OutlinedButton(
                            onClick = { showEntryPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IrishGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        ) { Text("Set entry date", fontSize = 12.sp) }
                    }

                    if (hasEntered) {
                        HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                        ResultRow("📅 Must leave Ireland by", mustLeaveBy.fmtVv(),
                            note = if (isOverstayed) "OVERSTAYED by ${ChronoUnit.DAYS.between(mustLeaveBy, today)} days" else "${ChronoUnit.DAYS.between(today, mustLeaveBy)} days remaining")
                        ResultRow("⏱ Days used",      "${daysUsed} / $duration")
                        ResultRow("⏳ Days remaining", "${daysRemaining}")

                        Spacer(Modifier.height(10.dp))
                        val progress = (daysUsed.toFloat() / duration).coerceIn(0f, 1f)
                        val barColor = when {
                            isOverstayed         -> StatusRed
                            daysRemaining < 14   -> StatusAmber
                            else                 -> IrishGreen
                        }
                        Text("Stay duration used", style = MaterialTheme.typography.labelSmall, color = TextHint)
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(10.dp).background(DividerGreen, RoundedCornerShape(5.dp))) {
                            Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(barColor, RoundedCornerShape(5.dp)))
                        }
                    }

                    if (entryType != EntryType.SINGLE) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = DividerGreen)
                        Spacer(Modifier.height(8.dp))
                        val entriesLabel = if (entryType == EntryType.DOUBLE) "2 entries" else "unlimited entries"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IrishGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                        ) {
                            Text("ℹ️ Your visa allows $entriesLabel. Each entry starts a new stay period, but total time in Ireland within the visa validity cannot exceed ${duration} days.", style = MaterialTheme.typography.bodySmall, color = IrishGreenDark)
                        }
                    }
                }
            }

            AccentCard(title = "⚠️  IMPORTANT NOTES", accentColor = IrishOrange) {
                Text(
                    "Always check your visa sticker carefully. The dates shown are based on what you enter here — verify with your actual sticker and the Official Ireland Immigration website (irishimmigration.ie). If in doubt about your status, contact the Irish Naturalisation and Immigration Service (INIS).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DateField(label: String, date: LocalDate?, onPick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextHint)
            Text(
                date?.fmtVv() ?: "Not set",
                fontWeight = FontWeight.SemiBold,
                color = if (date == null) TextHint else IrishGreenDark,
                fontSize = 15.sp,
            )
        }
        OutlinedButton(
            onClick = onPick,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, IrishGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = IrishGreen),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) { Text(if (date == null) "Pick" else "Change", fontSize = 12.sp) }
    }
}

@Composable
private fun ResultRow(label: String, value: String, note: String = "") {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(value, fontWeight = FontWeight.Bold, color = IrishGreenDark, fontSize = 14.sp)
        }
        if (note.isNotBlank()) {
            Text(note, style = MaterialTheme.typography.labelSmall, color = TextHint)
        }
    }
}

@Composable
private fun AlertBanner(title: String, body: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 13.sp)
            Text(body, style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}
