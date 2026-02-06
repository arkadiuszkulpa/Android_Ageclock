package com.ageclock.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ageclock.app.ai.ModelManager
import com.ageclock.app.data.local.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isModelDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadedMB: Int = 0,
    val totalMB: Int = ModelManager.MODEL_SIZE_MB,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val availableStorageMB: Long = 0,
    val hasEnoughStorage: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application)

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
}
