package com.ageclock.app.ui.addperson.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ageclock.app.data.model.AgeUnits
import com.ageclock.app.ui.theme.AgeclockTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UnitSelector(
    selectedUnits: Int,
    onUnitsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Display Units",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            AgeUnits.ALL_UNITS.forEach { (unit, name) ->
                val isChecked = AgeUnits.hasUnit(selectedUnits, unit)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            val newUnits = if (checked) {
                                selectedUnits or unit
                            } else {
                                // Don't allow unchecking if it's the only one
                                val newFlags = selectedUnits and unit.inv()
                                if (newFlags == 0) selectedUnits else newFlags
                            }
                            onUnitsChanged(newUnits)
                        }
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Select one for total, multiple for breakdown",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UnitSelectorPreview() {
    AgeclockTheme {
        UnitSelector(
            selectedUnits = AgeUnits.YEARS_MONTHS_DAYS,
            onUnitsChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UnitSelectorSinglePreview() {
    AgeclockTheme {
        UnitSelector(
            selectedUnits = AgeUnits.DAYS,
            onUnitsChanged = {}
        )
    }
}
