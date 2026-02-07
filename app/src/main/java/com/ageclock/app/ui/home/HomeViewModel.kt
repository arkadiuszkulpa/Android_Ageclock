package com.ageclock.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ageclock.app.ai.ModelManager
import com.ageclock.app.ai.PromptBuilder
import com.ageclock.app.data.local.AgeclockDatabase
import com.ageclock.app.data.local.SettingsDataStore
import com.ageclock.app.data.model.Person
import com.ageclock.app.data.repository.PersonRepository
import com.ageclock.app.util.AgeCalculator
import com.ageclock.app.widget.AgeWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AgeclockDatabase.getInstance(application)
    private val repository = PersonRepository(database.personDao())
    private val settingsDataStore = SettingsDataStore(application)

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
        showInWidget: Boolean,
        description: String? = null
    ) {
        viewModelScope.launch {
            val existingPerson = _selectedPerson.value
            val savedPerson: Person

            if (existingPerson != null) {
                // Update existing
                // Clear AI messages if display units changed (they need to be regenerated)
                val displayUnitsChanged = existingPerson.displayUnits != displayUnits
                savedPerson = existingPerson.copy(
                    name = name,
                    dateOfBirth = dateOfBirth,
                    displayUnits = displayUnits,
                    showInWidget = showInWidget,
                    description = description,
                    // Clear messages if units changed so they get regenerated
                    aiMessages = if (displayUnitsChanged) null else existingPerson.aiMessages,
                    aiMessagesGeneratedAt = if (displayUnitsChanged) null else existingPerson.aiMessagesGeneratedAt
                )
                repository.update(savedPerson)
            } else {
                // Insert new
                savedPerson = Person(
                    name = name,
                    dateOfBirth = dateOfBirth,
                    displayUnits = displayUnits,
                    showInWidget = showInWidget,
                    description = description
                )
                repository.insert(savedPerson)
            }
            dismissDialog()

            // Trigger AI message generation if enabled and model is downloaded
            // This will also notify widget update after messages are generated
            generateAiMessagesForPerson(savedPerson)

            // Also notify widget immediately for the basic person data update
            notifyWidgetUpdate()
        }
    }

    /**
     * Generate AI messages for a person using fallback template messages.
     * Messages are context-aware based on the person's description and use
     * the exact display units the user selected.
     */
    private fun generateAiMessagesForPerson(person: Person) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val aiEnabled = settingsDataStore.aiMessagesEnabled.first()

            if (!aiEnabled || !ModelManager.isModelDownloaded(context)) {
                return@launch
            }

            try {
                withContext(Dispatchers.IO) {
                    val age = AgeCalculator.calculateAge(person.dateOfBirth)
                    val messages = PromptBuilder.buildSimpleFallbackMessages(person, age)

                    // Get fresh person from DB (in case ID was auto-generated)
                    val freshPerson = repository.getByName(person.name)
                    if (freshPerson != null) {
                        val messagesJson = Json.encodeToString(messages)
                        repository.update(
                            freshPerson.copy(
                                aiMessages = messagesJson,
                                aiMessagesGeneratedAt = System.currentTimeMillis()
                            )
                        )
                        notifyWidgetUpdate()
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash - fallback to standard display
            }
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
