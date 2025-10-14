package com.example.zerowaste.ui.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This data class holds the state for the Settings screen
data class SettingsUiState(
    val is2faEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val logoutCompleted: Boolean = false
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadUserSettings()
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            // In a real app, you would fetch the user's current settings from your backend.
            // We'll simulate a network call.
            delay(500)
            _uiState.value = SettingsUiState(
                is2faEnabled = false, // Placeholder value
                isLoading = false
            )
        }
    }

    fun on2faToggleChanged(isEnabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // In a real app, you would call your API here to update the user's 2FA setting.
                // For example: userRepository.update2fa(isEnabled)
                delay(1000) // Simulate network delay for the update

                // Update the state on success
                _uiState.value = _uiState.value.copy(
                    is2faEnabled = isEnabled,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Handle errors and revert the state if the API call fails
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update setting. Please try again."
                )
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            // In a real app, you might call a logout endpoint on your backend.
            // You would also clear any locally stored data (like the JWT).
            // For now, we'll just update the state to trigger navigation.
            _uiState.value = _uiState.value.copy(logoutCompleted = true)
        }
    }
}
