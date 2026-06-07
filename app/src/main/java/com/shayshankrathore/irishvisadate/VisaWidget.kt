package com.shayshankrathore.irishvisadate

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class VisaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val saved = AppPreferences.flow(context).first()
        val today = LocalDate.now()

        val statusLine: String
        val dateLine: String

        if (saved.submissionDate == null) {
            statusLine = "No application tracked"
            dateLine   = "Open app to set up"
        } else {
            val embassy  = ALL_EMBASSIES.find { it.id == saved.embassyId } ?: ALL_EMBASSIES[0]
            val vac      = embassy.vacOptions.find { it.label == saved.vacLabel } ?: embassy.vacOptions[0]
            val visaType = runCatching {
                com.shayshankrathore.irishvisadate.ui.VisaType.valueOf(
                    saved.visaTypeName ?: "SHORT_STAY"
                )
            }.getOrDefault(com.shayshankrathore.irishvisadate.ui.VisaType.SHORT_STAY)

            val submission = runCatching { LocalDate.parse(saved.submissionDate) }.getOrNull()
            if (submission == null) {
                statusLine = "Invalid date"; dateLine = ""
            } else {
                val holidays = embassy.holidays
                val receive  = submission.addWorkingDays(vac.transitDays, holidays)
                val earliest = receive.addWorkingDays(visaType.minDays, holidays)
                val latest   = receive.addWorkingDays(visaType.maxDays, holidays)
                dateLine = "${earliest.monthValue}/${earliest.dayOfMonth} – ${latest.monthValue}/${latest.dayOfMonth}"
                statusLine = when {
                    today.isAfter(latest)    -> "Overdue"
                    today.isBefore(earliest) -> "Window in ${workingDaysBetween(today, earliest, holidays)} days"
                    else                     -> "${workingDaysBetween(today, latest, holidays)} days left"
                }
            }
        }

        provideContent { WidgetContent(statusLine, dateLine) }
    }
}

@Composable
private fun WidgetContent(statusLine: String, dateLine: String) {
    val isOverdue  = statusLine.startsWith("Overdue")
    val statusColor = if (isOverdue) Color(0xFFFF8F00) else Color(0xFF4CAF50)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF063320))
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(10.dp),
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                Text(
                    "☘️ Irish Visa Tracker",
                    style = TextStyle(
                        color = ColorProvider(Color(0xCCFFFFFF)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = GlanceModifier.defaultWeight(),
                )
            }
            Spacer(GlanceModifier.height(6.dp))
            Text(
                statusLine,
                style = TextStyle(
                    color = ColorProvider(statusColor),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
                maxLines = 1,
            )
            if (dateLine.isNotBlank()) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    dateLine,
                    style = TextStyle(
                        color = ColorProvider(Color(0xCCFFFFFF)),
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                )
            }
            Spacer(GlanceModifier.defaultWeight())
            Text(
                "Tap to open",
                style = TextStyle(
                    color = ColorProvider(Color(0x88FFFFFF)),
                    fontSize = 9.sp,
                ),
            )
        }
    }
}

class VisaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VisaWidget()
}
