package com.example.zerowaste.ui.passwordreset

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.ApiErrorResponse
import com.example.zerowaste.data.remote.PasswordResetExecute
import com.example.zerowaste.data.remote.PasswordResetRequest
import com.example.zerowaste.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// The sealed class with the new states
sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState() // For showing a spinner
    object CodeSent : PasswordResetState() // An intermediate step to inform the user
    object AwaitingCode : PasswordResetState()
    object ResetSuccess : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

class PasswordResetViewModel(application: Application) : AndroidViewModel(application) {

    // --- THIS IS THE FIX ---
    // Use the correct method from your new RetrofitClient
    private val apiService = RetrofitClient.getAuthApi(application)

    private val _uiState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val uiState: StateFlow<PasswordResetState> = _uiState

    private var tempEmail: String? = null

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.value = PasswordResetState.Loading
            try {
                apiService.requestPasswordReset(PasswordResetRequest(email))
                tempEmail = email
                _uiState.value = PasswordResetState.CodeSent
            } catch (e: Exception) {
                _uiState.value = PasswordResetState.Error(getApiErrorMessage(e))
            }
        }
    }

    fun proceedToEnterCode() {
        _uiState.value = PasswordResetState.AwaitingCode
    }

    fun executePasswordReset(code: String, newPassword: String) {
        val email = tempEmail ?: run {
            _uiState.value = PasswordResetState.Error("Email not found. Please start over.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PasswordResetState.Loading
            try {
                val request = PasswordResetExecute(email = email, code = code, newPassword = newPassword)
                apiService.executePasswordReset(request)
                _uiState.value = PasswordResetState.ResetSuccess
            } catch (e: Exception) {
                _uiState.value = PasswordResetState.Error(getApiErrorMessage(e))
            }
        }
    }

    fun resetFlow() {
        tempEmail = null
        _uiState.value = PasswordResetState.Idle
    }

    private fun getApiErrorMessage(e: Exception): String {
        return when (e) {
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    apiError.errorCode ?: "An error occurred."
                } catch (jsonError: Exception) { "Failed to parse error response." }
            }
            is IOException -> "Could not connect to the server."
            else -> e.message ?: "An unknown error occurred."
        }
    }
}

