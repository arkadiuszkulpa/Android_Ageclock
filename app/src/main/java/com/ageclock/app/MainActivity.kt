package com.ageclock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.ageclock.app.data.local.SettingsDataStore
import com.ageclock.app.ui.home.HomeScreen
import com.ageclock.app.ui.settings.SettingsScreen
import com.ageclock.app.ui.theme.AgeclockTheme
import com.ageclock.app.widget.AgeWidgetProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        settingsDataStore = SettingsDataStore(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgeclockTheme {
                var showSettings by remember { mutableStateOf(false) }

                if (showSettings) {
                    SettingsScreen(
                        onNavigateBack = { showSettings = false }
                    )
                } else {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        onNavigateToSettings = { showSettings = true }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Increment message index to rotate AI messages on each app resume
        lifecycleScope.launch {
            settingsDataStore.incrementMessageIndex()
        }
        // Update widget when user returns to the app
        AgeWidgetProvider.notifyWidgetDataChanged(this)
    }
}
