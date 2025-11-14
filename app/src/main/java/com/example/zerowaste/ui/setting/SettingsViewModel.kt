package com.example.zerowaste.ui.screens.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.ApiErrorResponse
import com.example.zerowaste.data.remote.RetrofitClient
import com.example.zerowaste.data.remote.Verify2faSetupRequest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// NEW: A sealed class to represent the different stages of the 2FA setup process
sealed class TwoFactorSetupState {
    object Idle : TwoFactorSetupState() // Not started
    object AwaitingVerification : TwoFactorSetupState() // Waiting for user to enter code
    data class Error(val message: String) : TwoFactorSetupState()
}

data class SettingsUiState(
    val is2faEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val logoutCompleted: Boolean = false,
    val twoFactorSetupState: TwoFactorSetupState = TwoFactorSetupState.Idle // Add the new state
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.getAuthApi(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun clearTwoFactorState() {
        _uiState.value = _uiState.value.copy(twoFactorSetupState = TwoFactorSetupState.Idle)
    }

    fun loadUserSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, twoFactorSetupState = TwoFactorSetupState.Error("User not logged in."))
                return@launch
            }
            try {
                val response = apiService.getUserDetails(userId)
                _uiState.value = _uiState.value.copy(
                    is2faEnabled = response.data.twoFactorAuthEnabled,
                    isLoading = false
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is JsonSyntaxException -> "Error parsing server response."
                    is HttpException -> "Error from server: HTTP ${e.code()}"
                    is IOException -> "Network error."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    twoFactorSetupState = TwoFactorSetupState.Error(errorMessage)
                )
            }
        }
    }

    // Called when the user clicks the toggle to turn 2FA ON
    fun onEnable2faClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, twoFactorSetupState = TwoFactorSetupState.Error("User ID not found."))
                return@launch
            }
            try {
                apiService.enable2fa(userId)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    twoFactorSetupState = TwoFactorSetupState.AwaitingVerification
                )
            } catch (e: Exception) {
                // --- THIS BLOCK IS NOW MORE DETAILED FOR DEBUGGING ---
                // We will print the full error to the console to see what it is
                Log.e("SettingsViewModel", "Error enabling 2FA", e)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    twoFactorSetupState = TwoFactorSetupState.Error(getApiErrorMessage(e))
                )
            }
        }
    }

    fun onDisable2faClicked() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, twoFactorSetupState = TwoFactorSetupState.Error("User ID not found."))
                return@launch
            }

            try {
                // Call the new API endpoint
                apiService.disable2fa(userId)

                // On success, update the toggle to OFF
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    is2faEnabled = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    twoFactorSetupState = TwoFactorSetupState.Error(getApiErrorMessage(e))
                )
            }
        }
    }

    // Called when the user submits the code from their email
    fun onVerify2faSetup(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // 1. Get the userId from the session
            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, twoFactorSetupState = TwoFactorSetupState.Error("User ID not found. Please log in again."))
                return@launch
            }
            try {
                val request = Verify2faSetupRequest(verificationCode = code)
                // 2. Call the API with the userId and the request body
                apiService.verify2faSetup(userId, request)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    is2faEnabled = true,
                    twoFactorSetupState = TwoFactorSetupState.Idle
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    twoFactorSetupState = TwoFactorSetupState.Error(getApiErrorMessage(e))
                )
            }
        }
    }

    // Called when the user wants to go back from the verification screen
    fun cancel2faSetup() {
        _uiState.value = _uiState.value.copy(twoFactorSetupState = TwoFactorSetupState.Idle)
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.value = _uiState.value.copy(logoutCompleted = true)
        }
    }

    fun resetLogoutState() {
        _uiState.value = _uiState.value.copy(logoutCompleted = false)
    }

    private fun getApiErrorMessage(e: Exception): String {
        return when (e) {
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    apiError.errorCode
                } catch (jsonError: Exception) { "Failed to parse error response." }
            }
            is IOException -> "Could not connect to the server."
            else -> e.message ?: "An unknown error occurred."
        }
    }
}

