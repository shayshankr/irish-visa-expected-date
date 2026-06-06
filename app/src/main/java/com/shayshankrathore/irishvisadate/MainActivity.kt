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

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                var screen           by remember { mutableStateOf(AppScreen.TRACKER) }
                var checklistType    by remember { mutableStateOf(VisaType.SHORT_STAY) }
                var decisionsUrl     by remember { mutableStateOf("") }
                var trackerReloadKey by remember { mutableStateOf(0) }

                BackHandler(enabled = screen != AppScreen.TRACKER) {
                    screen = AppScreen.TRACKER
                }

                when (screen) {
                    AppScreen.TRACKER -> key(trackerReloadKey) {
                        VisaTrackerScreen(
                            onNavigate       = { screen = it },
                            onOpenChecklist  = { vt -> checklistType = vt; screen = AppScreen.CHECKLIST },
                            onOpenDecisions  = { url -> decisionsUrl = url; screen = AppScreen.DECISIONS_WEB },
                        )
                    }
                    AppScreen.GRANTED ->
                        VisaGrantedScreen(onBack = { screen = AppScreen.TRACKER })
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
                }
            }
        }
    }
}
