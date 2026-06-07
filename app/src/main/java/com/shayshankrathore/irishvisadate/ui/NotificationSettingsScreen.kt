package com.shayshankrathore.irishvisadate.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shayshankrathore.irishvisadate.AppPreferences
import com.shayshankrathore.irishvisadate.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()
    val settings by AppPreferences.notificationSettingsFlow(context)
        .collectAsState(initial = AppPreferences.NotificationSettings())

    Scaffold(
        containerColor = IrishGreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings", fontWeight = FontWeight.ExtraBold) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AccentCard(title = "🔔  DECISION WINDOW REMINDERS", accentColor = IrishOrange) {
                Text(
                    "Choose which reminders to receive. They are automatically rescheduled when you change any input on the tracker.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))

                NotifToggle(
                    title    = "Window opens tomorrow",
                    subtitle = "Notified the day before your earliest expected decision date",
                    checked  = settings.windowOpens,
                    onCheckedChange = {
                        scope.launch {
                            AppPreferences.saveNotificationSettings(context, settings.copy(windowOpens = it))
                        }
                    },
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                NotifToggle(
                    title    = "Halfway through window",
                    subtitle = "Notified at the midpoint of your decision window",
                    checked  = settings.midpoint,
                    onCheckedChange = {
                        scope.launch {
                            AppPreferences.saveNotificationSettings(context, settings.copy(midpoint = it))
                        }
                    },
                )
                HorizontalDivider(color = DividerGreen, modifier = Modifier.padding(vertical = 8.dp))
                NotifToggle(
                    title    = "Application may be overdue",
                    subtitle = "Notified the day after your latest expected decision date",
                    checked  = settings.overdue,
                    onCheckedChange = {
                        scope.launch {
                            AppPreferences.saveNotificationSettings(context, settings.copy(overdue = it))
                        }
                    },
                )
            }

            AccentCard(title = "🔍  DECISIONS PAGE WATCHDOG", accentColor = IrishGreen) {
                Text(
                    "Periodically checks the embassy's decisions page every 4 hours and notifies you when new decisions are published. Check if your name appears.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Spacer(Modifier.height(12.dp))
                NotifToggle(
                    title    = "Decisions page watchdog",
                    subtitle = "Get alerted when the embassy decisions list is updated",
                    checked  = settings.watchdog,
                    onCheckedChange = {
                        scope.launch {
                            AppPreferences.saveNotificationSettings(context, settings.copy(watchdog = it))
                        }
                    },
                )
            }

            AccentCard(title = "ℹ️  HOW IT WORKS", accentColor = IrishGreen) {
                Text(
                    "Reminders are scheduled as one-time background tasks via Android WorkManager. They reset automatically whenever you change your embassy, VAC, date, or visa type. Uninstalling the app cancels all pending reminders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun NotifToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = IrishGreenDark, fontSize = 14.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = IrishGreen,
                checkedTrackColor = IrishGreen.copy(alpha = 0.4f),
            ),
        )
    }
}
