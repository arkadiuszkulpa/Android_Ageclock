package com.ageclock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.ageclock.app.ui.ClockScreen
import com.ageclock.app.ui.theme.AgeclockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgeclockTheme {
                ClockScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
