package com.ageclock.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ageclock.app.ai.MessageGenerator
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
    private var messageGenerator: MessageGenerator? = null

    private val _isGeneratingMessages = MutableStateFlow(false)
    val isGeneratingMessages: StateFlow<Boolean> = _isGeneratingMessages.asStateFlow()

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
                savedPerson = existingPerson.copy(
                    name = name,
                    dateOfBirth = dateOfBirth,
                    displayUnits = displayUnits,
                    showInWidget = showInWidget,
                    description = description
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
            notifyWidgetUpdate()

            // Trigger AI message generation if enabled and model is downloaded
            generateAiMessagesForPerson(savedPerson)
        }
    }

    private fun generateAiMessagesForPerson(person: Person) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val aiEnabled = settingsDataStore.aiMessagesEnabled.first()

            if (!aiEnabled || !ModelManager.isModelDownloaded(context)) {
                return@launch
            }

            _isGeneratingMessages.value = true

            try {
                withContext(Dispatchers.IO) {
                    // Initialize generator if needed
                    if (messageGenerator == null) {
                        messageGenerator = MessageGenerator(context)
                        messageGenerator?.initialize()
                    }

                    val generator = messageGenerator
                    if (generator != null && generator.isReady()) {
                        val age = AgeCalculator.calculateAge(person.dateOfBirth)
                        val messages = generator.generateMessages(person, age)

                        if (messages.isNotEmpty()) {
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
                    } else {
                        // Fall back to simple messages if LLM not ready
                        val age = AgeCalculator.calculateAge(person.dateOfBirth)
                        val fallbackMessages = PromptBuilder.buildSimpleFallbackMessages(person, age)

                        val freshPerson = repository.getByName(person.name)
                        if (freshPerson != null) {
                            val messagesJson = Json.encodeToString(fallbackMessages)
                            repository.update(
                                freshPerson.copy(
                                    aiMessages = messagesJson,
                                    aiMessagesGeneratedAt = System.currentTimeMillis()
                                )
                            )
                            notifyWidgetUpdate()
                        }
                    }
                }
            } catch (e: Exception) {
                // Log error but don't crash - fallback to standard display
            } finally {
                _isGeneratingMessages.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageGenerator?.release()
        messageGenerator = null
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
