package com.ageclock.app.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ageclock.app.data.model.AgeUnits
import com.ageclock.app.data.model.Person
import com.ageclock.app.ui.theme.AgeclockTheme
import com.ageclock.app.util.AgeCalculator
import kotlinx.coroutines.delay

@Composable
fun PersonTile(
    person: Person,
    onClick: () -> Unit,
    onToggleWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Update interval based on selected units
    val updateInterval = when {
        AgeUnits.hasUnit(person.displayUnits, AgeUnits.SECONDS) -> 1000L
        AgeUnits.hasUnit(person.displayUnits, AgeUnits.MINUTES) -> 60_000L
        AgeUnits.hasUnit(person.displayUnits, AgeUnits.HOURS) -> 60_000L
        else -> 60_000L // Update every minute for day-based granularities
    }

    LaunchedEffect(updateInterval) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(updateInterval)
        }
    }

    val age = remember(person.dateOfBirth, currentTimeMillis) {
        AgeCalculator.calculateAge(person.dateOfBirth, currentTimeMillis)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with first letter
            PersonAvatar(name = person.name)

            Spacer(modifier = Modifier.width(16.dp))

            // Name and age
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = age.format(person.displayUnits),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Widget toggle
            IconButton(onClick = onToggleWidget) {
                Icon(
                    imageVector = if (person.showInWidget) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                    contentDescription = if (person.showInWidget) "Remove from widget" else "Add to widget",
                    tint = if (person.showInWidget)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun PersonAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercase() ?: "?"

    Surface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonTilePreview() {
    AgeclockTheme {
        PersonTile(
            person = Person(
                id = 1,
                name = "Emma",
                dateOfBirth = System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000),
                displayUnits = AgeUnits.YEARS_MONTHS_DAYS,
                showInWidget = true
            ),
            onClick = {},
            onToggleWidget = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonTileSecondsPreview() {
    AgeclockTheme {
        PersonTile(
            person = Person(
                id = 2,
                name = "Grandpa Joe",
                dateOfBirth = System.currentTimeMillis() - (82L * 365 * 24 * 60 * 60 * 1000),
                displayUnits = AgeUnits.SECONDS,
                showInWidget = false
            ),
            onClick = {},
            onToggleWidget = {}
        )
    }
}
