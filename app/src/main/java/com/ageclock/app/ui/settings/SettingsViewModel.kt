package com.ageclock.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ageclock.app.ai.MessageGenerator
import com.ageclock.app.ai.ModelManager
import com.ageclock.app.ai.PromptBuilder
import com.ageclock.app.data.local.AgeclockDatabase
import com.ageclock.app.data.local.SettingsDataStore
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

data class SettingsUiState(
    val isModelDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedMB: Int = 0,
    val totalMB: Int = ModelManager.MODEL_SIZE_MB,
    val isDeleting: Boolean = false,
    val isRegenerating: Boolean = false,
    val regenerateProgress: Float = 0f,
    val regenerateTotal: Int = 0,
    val regenerateCurrent: Int = 0,
    val errorMessage: String? = null,
    val availableStorageMB: Long = 0,
    val hasEnoughStorage: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)
    private val database = AgeclockDatabase.getInstance(application)
    private val repository = PersonRepository(database.personDao())

    val aiMessagesEnabled: StateFlow<Boolean> = settingsDataStore.aiMessagesEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        checkModelStatus()
    }

    fun checkModelStatus() {
        val context = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            isModelDownloaded = ModelManager.isModelDownloaded(context),
            availableStorageMB = ModelManager.getAvailableStorageMB(context),
            hasEnoughStorage = ModelManager.hasEnoughStorage(context)
        )
    }

    fun downloadModel() {
        if (_uiState.value.isDownloading) return

        val context = getApplication<Application>()

        if (!ModelManager.hasEnoughStorage(context)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Not enough storage. Need at least ${ModelManager.MODEL_SIZE_MB + 100}MB free."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                downloadProgress = 0f,
                downloadedMB = 0,
                errorMessage = null
            )

            val result = ModelManager.downloadModel(context) { progress, downloadedMB, totalMB ->
                _uiState.value = _uiState.value.copy(
                    downloadProgress = progress,
                    downloadedMB = downloadedMB,
                    totalMB = totalMB
                )
            }

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        isModelDownloaded = true,
                        downloadProgress = 1f
                    )
                    // Enable AI messages after successful download
                    settingsDataStore.setAiMessagesEnabled(true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        errorMessage = "Download failed: ${error.message}"
                    )
                }
            )
        }
    }

    fun deleteModel() {
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            val success = ModelManager.deleteModel(getApplication())

            _uiState.value = _uiState.value.copy(
                isDeleting = false,
                isModelDownloaded = !success
            )

            if (success) {
                settingsDataStore.setAiMessagesEnabled(false)
            }

            checkModelStatus()
        }
    }

    fun setAiMessagesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAiMessagesEnabled(enabled)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun regenerateAllMessages() {
        if (_uiState.value.isRegenerating) return

        viewModelScope.launch {
            val context = getApplication<Application>()
            val aiEnabled = settingsDataStore.aiMessagesEnabled.first()

            if (!aiEnabled || !ModelManager.isModelDownloaded(context)) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "AI messages must be enabled and model downloaded"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isRegenerating = true,
                regenerateProgress = 0f,
                regenerateCurrent = 0
            )

            try {
                withContext(Dispatchers.IO) {
                    val allPeople = repository.allPeople.first()
                    val total = allPeople.size

                    _uiState.value = _uiState.value.copy(regenerateTotal = total)

                    allPeople.forEachIndexed { index, person ->
                        val age = AgeCalculator.calculateAge(person.dateOfBirth)
                        val messages = PromptBuilder.buildSimpleFallbackMessages(person, age)
                        val messagesJson = Json.encodeToString(messages)

                        repository.update(
                            person.copy(
                                aiMessages = messagesJson,
                                aiMessagesGeneratedAt = System.currentTimeMillis()
                            )
                        )

                        _uiState.value = _uiState.value.copy(
                            regenerateCurrent = index + 1,
                            regenerateProgress = (index + 1).toFloat() / total
                        )
                    }

                    // Notify widget to update
                    AgeWidgetProvider.notifyWidgetDataChanged(context)
                }

                _uiState.value = _uiState.value.copy(
                    isRegenerating = false,
                    regenerateProgress = 1f
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRegenerating = false,
                    errorMessage = "Regeneration failed: ${e.message}"
                )
            }
        }
    }
}
