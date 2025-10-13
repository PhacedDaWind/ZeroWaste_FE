package com.example.zerowaste.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.ApiErrorResponse // Ensure this import is present
import com.example.zerowaste.data.remote.LoginRequest
import com.example.zerowaste.data.remote.LoginResponse
import com.example.zerowaste.data.remote.RetrofitClient
import com.example.zerowaste.data.remote.Verify2faRequest
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

enum class LoginUiState {
    EnteringCredentials,
    Entering2faCode
}

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState.EnteringCredentials)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _finalLoginResult = MutableLiveData<Result<String>>()
    val finalLoginResult: LiveData<Result<String>> = _finalLoginResult

    private var tempUsername: String? = null

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password)
                val response = RetrofitClient.authApiService.login(request)

                if (response.status == "2FA_REQUIRED") {
                    tempUsername = username
                    _uiState.value = LoginUiState.Entering2faCode
                } else if (response.status == "SUCCESS" && response.token != null) {
                    _finalLoginResult.postValue(Result.success(response.token))
                } else {
                    _finalLoginResult.postValue(Result.failure(Exception("Unexpected response from server.")))
                }
            } catch (e: Exception) {
                handleApiError(e) // Use the corrected handler
            }
        }
    }

    fun verify2faCode(code: String) {
        val username = tempUsername ?: run {
            _finalLoginResult.postValue(Result.failure(Exception("Username is missing.")))
            return
        }
        viewModelScope.launch {
            try {
                val request = Verify2faRequest(username = username, code = code)
                val response = RetrofitClient.authApiService.verify2fa(request)
                if (response.status == "SUCCESS" && response.token != null) {
                    _finalLoginResult.postValue(Result.success(response.token))
                    _uiState.value = LoginUiState.EnteringCredentials
                } else {
                    _finalLoginResult.postValue(Result.failure(Exception(response.message)))
                }
            } catch (e: Exception) {
                handleApiError(e) // Use the corrected handler
            }
        }
    }

    // --- THIS FUNCTION IS NOW FIXED ---
    private fun handleApiError(e: Exception) {
        val errorMessage = when (e) {
            is HttpException -> {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    // CORRECTED: Use the ApiErrorResponse class to parse the error
                    val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                    apiError.errorCode // Now it correctly gets the errorCode
                } catch (jsonError: Exception) {
                    "Failed to parse error response."
                }
            }
            is IOException -> "Could not connect to the server."
            else -> e.message ?: "An unknown error occurred."
        }
        _finalLoginResult.postValue(Result.failure(Exception(errorMessage)))
    }
}

