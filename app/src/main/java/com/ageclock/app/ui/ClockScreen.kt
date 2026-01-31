package com.ageclock.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ageclock.app.ui.theme.AgeclockTheme
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ClockScreen(
    modifier: Modifier = Modifier
) {
    // State to trigger recomposition every second
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Update the time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    // Get current time - recalculates when currentTimeMillis changes
    val now = remember(currentTimeMillis) { LocalDateTime.now() }

    // Format time as HH:MM:SS
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()) }
    val timeText = now.format(timeFormatter)

    // Format date as "Saturday, January 25, 2026"
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val dateText = now.format(dateFormatter)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Time display
            Text(
                text = timeText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date display
            Text(
                text = dateText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
private fun ClockScreenPreviewLight() {
    AgeclockTheme(darkTheme = false) {
        ClockScreen()
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
private fun ClockScreenPreviewDark() {
    AgeclockTheme(darkTheme = true) {
        ClockScreen()
    }
}
