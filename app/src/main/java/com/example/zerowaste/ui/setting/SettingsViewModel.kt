package com.example.zerowaste.ui.screens.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val is2faEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val logoutCompleted: Boolean = false
)

// 1. Change ViewModel to AndroidViewModel
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // 2. Create an instance of SessionManager
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadUserSettings()
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            delay(500)
            _uiState.value = SettingsUiState(
                is2faEnabled = false,
                isLoading = false
            )
        }
    }

    fun on2faToggleChanged(isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    is2faEnabled = isEnabled,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update setting."
                )
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // 3. CLEAR THE SESSION DATA
            sessionManager.clearSession()
            // Then update the UI state to trigger navigation
            _uiState.value = _uiState.value.copy(logoutCompleted = true)
        }
    }

    fun resetLogoutState() {
        _uiState.value = _uiState.value.copy(logoutCompleted = false)
    }
}

