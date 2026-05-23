package com.shayshankrathore.irishvisadate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shayshankrathore.irishvisadate.ui.AppScreen
import com.shayshankrathore.irishvisadate.ui.VisaGrantedScreen
import com.shayshankrathore.irishvisadate.ui.VisaRefusedScreen
import com.shayshankrathore.irishvisadate.ui.VisaTrackerScreen
import com.shayshankrathore.irishvisadate.ui.theme.IrishVisaExpectedDateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IrishVisaExpectedDateTheme {
                var screen by remember { mutableStateOf(AppScreen.TRACKER) }

                BackHandler(enabled = screen != AppScreen.TRACKER) {
                    screen = AppScreen.TRACKER
                }

                when (screen) {
                    AppScreen.TRACKER -> VisaTrackerScreen(onNavigate = { screen = it })
                    AppScreen.GRANTED -> VisaGrantedScreen(onBack = { screen = AppScreen.TRACKER })
                    AppScreen.REFUSED -> VisaRefusedScreen(onBack = { screen = AppScreen.TRACKER })
                }
            }
        }
    }
}
