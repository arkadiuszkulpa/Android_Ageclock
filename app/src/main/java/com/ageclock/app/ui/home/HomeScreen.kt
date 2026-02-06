package com.ageclock.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ageclock.app.data.model.AgeUnits
import com.ageclock.app.data.model.Person
import com.ageclock.app.ui.addperson.AddEditPersonDialog
import com.ageclock.app.ui.home.components.AboutBanner
import com.ageclock.app.ui.home.components.AppHeader
import com.ageclock.app.ui.home.components.ClockSection
import com.ageclock.app.ui.home.components.PersonTile
import com.ageclock.app.ui.theme.AgeclockTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {}
) {
    val people by viewModel.people.collectAsState()
    val showDialog by viewModel.showAddEditDialog.collectAsState()
    val selectedPerson by viewModel.selectedPerson.collectAsState()

    val hasPeople = people.isNotEmpty()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Person",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppHeader()
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    AboutBanner()
                }

                item {
                    ClockSection(compact = hasPeople)
                }

                items(
                    items = people,
                    key = { it.id }
                ) { person ->
                    PersonTile(
                        person = person,
                        onClick = { viewModel.showEditDialog(person) },
                        onToggleWidget = { viewModel.toggleWidgetDisplay(person) }
                    )
                }
            }
        }

        if (showDialog) {
            AddEditPersonDialog(
                person = selectedPerson,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { name, dateOfBirth, granularity, showInWidget, description ->
                    viewModel.savePerson(name, dateOfBirth, granularity, showInWidget, description)
                },
                onDelete = selectedPerson?.let { person ->
                    { viewModel.deletePerson(person) }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    AgeclockTheme {
        HomeScreenPreview(emptyList())
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenWithPeoplePreview() {
    AgeclockTheme {
        HomeScreenPreview(
            listOf(
                Person(
                    id = 1,
                    name = "Emma",
                    dateOfBirth = System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000),
                    displayUnits = AgeUnits.YEARS_MONTHS_DAYS,
                    showInWidget = true
                ),
                Person(
                    id = 2,
                    name = "Grandpa Joe",
                    dateOfBirth = System.currentTimeMillis() - (82L * 365 * 24 * 60 * 60 * 1000),
                    displayUnits = AgeUnits.YEARS_DAYS,
                    showInWidget = true
                )
            )
        )
    }
}

@Composable
private fun HomeScreenPreview(people: List<Person>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    AppHeader()
                }

                item {
                    AboutBanner()
                }

                item {
                    ClockSection(compact = people.isNotEmpty())
                }

                items(people, key = { it.id }) { person ->
                    PersonTile(
                        person = person,
                        onClick = {},
                        onToggleWidget = {}
                    )
                }
            }
        }
    }
}
