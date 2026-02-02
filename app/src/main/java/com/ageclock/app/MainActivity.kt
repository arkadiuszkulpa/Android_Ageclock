package com.ageclock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ageclock.app.ui.home.HomeScreen
import com.ageclock.app.ui.theme.AgeclockTheme
import com.ageclock.app.widget.AgeWidgetProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgeclockTheme {
                HomeScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Update widget when user returns to the app
        AgeWidgetProvider.notifyWidgetDataChanged(this)
    }
}
