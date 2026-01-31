package com.ageclock.app.ui.addperson

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ageclock.app.data.model.AgeUnits
import com.ageclock.app.data.model.Person
import com.ageclock.app.ui.addperson.components.UnitSelector
import com.ageclock.app.ui.theme.AgeclockTheme
import com.ageclock.app.util.AgeCalculator
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonDialog(
    person: Person?,
    onDismiss: () -> Unit,
    onSave: (name: String, dateOfBirth: Long, displayUnits: Int, showInWidget: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(person) { mutableStateOf(person?.name ?: "") }
    var selectedDateMillis by remember(person) { mutableStateOf(person?.dateOfBirth) }
    var selectedHour by remember(person) {
        mutableIntStateOf(
            person?.dateOfBirth?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).hour
            } ?: 0
        )
    }
    var selectedMinute by remember(person) {
        mutableIntStateOf(
            person?.dateOfBirth?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).minute
            } ?: 0
        )
    }
    var displayUnits by remember(person) {
        mutableIntStateOf(person?.displayUnits ?: AgeUnits.YEARS_MONTHS_DAYS)
    }
    var showInWidget by remember(person) { mutableStateOf(person?.showInWidget ?: false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isEditing = person != null
    val isValid by remember {
        derivedStateOf { name.isNotBlank() && selectedDateMillis != null }
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    }

    val formattedDate = selectedDateMillis?.let { millis ->
        AgeCalculator.millisToLocalDate(millis).format(dateFormatter)
    } ?: "Select date"

    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)

    // Combine date and time into final millis
    val finalDateTimeMillis: Long? = selectedDateMillis?.let { dateMillis ->
        val date = AgeCalculator.millisToLocalDate(dateMillis)
        val time = LocalTime.of(selectedHour, selectedMinute)
        val dateTime = LocalDateTime.of(date, time)
        dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (isEditing) "Edit Person" else "Add Person")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date of Birth
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    label = { Text("Date of Birth") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time of Birth (optional)
                OutlinedTextField(
                    value = formattedTime,
                    onValueChange = {},
                    label = { Text("Time of Birth (optional)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Select time"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Unit selector (checkboxes)
                UnitSelector(
                    selectedUnits = displayUnits,
                    onUnitsChanged = { displayUnits = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Widget toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Display in Widget",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = showInWidget,
                        onCheckedChange = { showInWidget = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    finalDateTimeMillis?.let { dateTimeMillis ->
                        onSave(name.trim(), dateTimeMillis, displayUnits, showInWidget)
                    }
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (isEditing && onDelete != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )

    // Date picker dialog
    if (showDatePicker) {
        val currentTimeMillis = System.currentTimeMillis()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: currentTimeMillis,
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= currentTimeMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateMillis = millis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time of Birth") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimeInput(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
private fun AddPersonDialogPreview() {
    AgeclockTheme {
        AddEditPersonDialog(
            person = null,
            onDismiss = {},
            onSave = { _, _, _, _ -> }
        )
    }
}

@Preview
@Composable
private fun EditPersonDialogPreview() {
    AgeclockTheme {
        AddEditPersonDialog(
            person = Person(
                id = 1,
                name = "Emma",
                dateOfBirth = System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000),
                displayUnits = AgeUnits.YEARS_MONTHS_DAYS,
                showInWidget = true
            ),
            onDismiss = {},
            onSave = { _, _, _, _ -> },
            onDelete = {}
        )
    }
}
