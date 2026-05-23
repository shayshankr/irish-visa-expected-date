package com.shayshankrathore.irishvisadate.ui

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shayshankrathore.irishvisadate.URL_APPEALS_INFO
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

private const val APPEAL_WINDOW_DAYS = 61L // 2 months ≈ 61 days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisaRefusedScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var refusalDate by remember { mutableStateOf<LocalDate?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    val todayMillis = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = refusalDate
            ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
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
                        refusalDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = pickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visa Refused") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("← Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "We're sorry to hear that. Here's what you can do:",
                style = MaterialTheme.typography.bodyLarge,
            )

            // ── Appeal section ────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APPEAL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("You can appeal within 2 months of your refusal date.")
                    Text(
                        text = "Send your appeal to the Visa Appeals Officer. Include a detailed letter addressing each specific reason for refusal, plus any new supporting documents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(12.dp))

                    if (refusalDate == null) {
                        OutlinedButton(onClick = { showPicker = true }) {
                            Text("Set refusal date to see deadline")
                        }
                    } else {
                        val deadline = refusalDate!!.plusDays(APPEAL_WINDOW_DAYS)
                        val daysLeft = ChronoUnit.DAYS.between(today, deadline)
                        val progress = ((APPEAL_WINDOW_DAYS - daysLeft).toFloat() / APPEAL_WINDOW_DAYS)
                            .coerceIn(0f, 1f)
                        val isExpired = daysLeft < 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = if (isExpired) "Appeal window expired"
                                           else "$daysLeft days left to appeal",
                                    fontWeight = FontWeight.Bold,
                                    color = if (daysLeft < 14) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "Deadline: $deadline",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { showPicker = true }) { Text("Change") }
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isExpired || daysLeft < 14) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { openCustomTab(context, URL_APPEALS_INFO) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Open appeals info")
                    }
                }
            }

            // ── Reapply section ───────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAPPLY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("You can submit a fresh application at any time, addressing the reasons for refusal with stronger supporting documents.")
                }
            }
        }
    }
}

internal fun openCustomTab(context: Context, url: String) {
    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
        .launchUrl(context, Uri.parse(url))
}
