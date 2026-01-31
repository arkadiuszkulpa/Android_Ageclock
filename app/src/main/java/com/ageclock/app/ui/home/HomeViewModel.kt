package com.ageclock.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ageclock.app.data.local.AgeclockDatabase
import com.ageclock.app.data.model.Person
import com.ageclock.app.data.repository.PersonRepository
import com.ageclock.app.widget.AgeWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AgeclockDatabase.getInstance(application)
    private val repository = PersonRepository(database.personDao())

    val people: StateFlow<List<Person>> = repository.allPeople
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedPerson = MutableStateFlow<Person?>(null)
    val selectedPerson: StateFlow<Person?> = _selectedPerson.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    fun showAddDialog() {
        _selectedPerson.value = null
        _showAddEditDialog.value = true
    }

    fun showEditDialog(person: Person) {
        _selectedPerson.value = person
        _showAddEditDialog.value = true
    }

    fun dismissDialog() {
        _showAddEditDialog.value = false
        _selectedPerson.value = null
    }

    fun savePerson(
        name: String,
        dateOfBirth: Long,
        displayUnits: Int,
        showInWidget: Boolean
    ) {
        viewModelScope.launch {
            val existingPerson = _selectedPerson.value
            if (existingPerson != null) {
                // Update existing
                repository.update(
                    existingPerson.copy(
                        name = name,
                        dateOfBirth = dateOfBirth,
                        displayUnits = displayUnits,
                        showInWidget = showInWidget
                    )
                )
            } else {
                // Insert new
                repository.insert(
                    Person(
                        name = name,
                        dateOfBirth = dateOfBirth,
                        displayUnits = displayUnits,
                        showInWidget = showInWidget
                    )
                )
            }
            dismissDialog()
            notifyWidgetUpdate()
        }
    }

    fun deletePerson(person: Person) {
        viewModelScope.launch {
            repository.delete(person)
            dismissDialog()
            notifyWidgetUpdate()
        }
    }

    fun toggleWidgetDisplay(person: Person) {
        viewModelScope.launch {
            repository.toggleWidgetDisplay(person)
            notifyWidgetUpdate()
        }
    }

    private fun notifyWidgetUpdate() {
        AgeWidgetProvider.notifyWidgetDataChanged(getApplication())
    }
}
