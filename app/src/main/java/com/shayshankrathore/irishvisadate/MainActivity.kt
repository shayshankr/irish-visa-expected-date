package com.shayshankrathore.irishvisadate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.shayshankrathore.irishvisadate.ui.*
import com.shayshankrathore.irishvisadate.ui.theme.IrishVisaExpectedDateTheme
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalizationManager.applyLanguageOnStartup(this)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            IrishVisaExpectedDateTheme {
                val context = LocalContext.current
                val scope   = rememberCoroutineScope()

                // Onboarding gate — null = still loading from DataStore
                val onboardingDone by AppPreferences.onboardingDoneFlow(context)
                    .collectAsState(initial = null)

                var screen           by remember { mutableStateOf(AppScreen.TRACKER) }
                var checklistType    by remember { mutableStateOf(VisaType.SHORT_STAY) }
                var decisionsUrl     by remember { mutableStateOf("") }
                var trackerReloadKey by remember { mutableStateOf(0) }

                BackHandler(enabled = screen != AppScreen.TRACKER) {
                    screen = AppScreen.TRACKER
                }

                when {
                    // Still reading DataStore — show nothing to avoid flicker
                    onboardingDone == null -> {}

                    // First-time user: show onboarding
                    onboardingDone == false -> {
                        OnboardingScreen(onComplete = {
                            scope.launch { AppPreferences.markOnboardingDone(context) }
                        })
                    }

                    // Normal flow
                    else -> when (screen) {
                        AppScreen.TRACKER -> key(trackerReloadKey) {
                            VisaTrackerScreen(
                                onNavigate      = { screen = it },
                                onOpenChecklist = { vt -> checklistType = vt; screen = AppScreen.CHECKLIST },
                                onOpenDecisions = { url -> decisionsUrl = url; screen = AppScreen.DECISIONS_WEB },
                            )
                        }
                        AppScreen.GRANTED ->
                            VisaGrantedScreen(
                                onBack = { screen = AppScreen.TRACKER },
                                onVisaValidity = { screen = AppScreen.VISA_VALIDITY },
                            )
                        AppScreen.REFUSED ->
                            VisaRefusedScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.CHECKLIST ->
                            VisaChecklistScreen(visaType = checklistType, onBack = { screen = AppScreen.TRACKER })
                        AppScreen.DECISIONS_WEB ->
                            DecisionsWebViewScreen(url = decisionsUrl, onBack = { screen = AppScreen.TRACKER })
                        AppScreen.APP_LIST ->
                            ApplicationListScreen(
                                onBack = { screen = AppScreen.TRACKER },
                                onLoadApplication = { app ->
                                    scope.launch {
                                        AppPreferences.save(
                                            context        = context,
                                            embassyId      = app.embassyId,
                                            vacLabel       = app.vacLabel,
                                            submissionDate = app.submissionDate,
                                            visaTypeName   = app.visaTypeName,
                                        )
                                        trackerReloadKey++
                                        screen = AppScreen.TRACKER
                                    }
                                },
                            )
                        AppScreen.FAQ ->
                            FaqScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.NOTIFICATION_SETTINGS ->
                            NotificationSettingsScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.BACKUP ->
                            BackupRestoreScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.TRAVEL_PLANNER ->
                            TravelPlannerScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.COMMUNITY_TIMES ->
                            CommunityTimesScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.DOC_TRACKER ->
                            DocumentTrackerScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.COST_CALCULATOR ->
                            CostCalculatorScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.VISA_VALIDITY ->
                            VisaValidityScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.LANGUAGE_SETTINGS ->
                            LanguageSettingsScreen(onBack = { screen = AppScreen.TRACKER })
                        AppScreen.APPLICATION_STATUS ->
                            ApplicationStatusScreen(onBack = { screen = AppScreen.TRACKER })
                    }
                }
            }
        }
    }
}
