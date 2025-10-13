package com.example.zerowaste.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.ApiErrorResponse
import com.example.zerowaste.data.remote.LoginRequest
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

    private var tempTwoFactorToken: String? = null

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password)
                val response = RetrofitClient.authApiService.login(request)

                if (response.twoFactorEnabled) {
                    tempTwoFactorToken = response.twoFactorToken
                    _uiState.value = LoginUiState.Entering2faCode
                } else {
                    response.token?.let { finalToken ->
                        _finalLoginResult.postValue(Result.success(finalToken))
                    } ?: _finalLoginResult.postValue(Result.failure(Exception("Token was not provided.")))
                }
            } catch (e: Exception) {
                // This is the new, smarter error handling block
                val errorMessage = when (e) {
                    is HttpException -> { // This is for errors from the server (like 400, 401, 500)
                        try {
                            // Attempt to parse the error body into our custom error object
                            val errorBody = e.response()?.errorBody()?.string()
                            val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)

                            // Now you can use the specific error code!
                            "Login Failed: ${apiError.errorCode}"

                        } catch (jsonError: Exception) {
                            // If parsing fails, fall back to a generic message based on the status code
                            when (e.code()) {
                                400 -> "Invalid request. Please check your input."
                                401 -> "Invalid username or password."
                                500 -> "A server error occurred. Please try again later."
                                else -> "An unexpected error occurred."
                            }
                        }
                    }
                    is IOException -> "Could not connect to the server. Please check your network."
                    else -> e.message ?: "An unknown error occurred."
                }
                _finalLoginResult.postValue(Result.failure(Exception(errorMessage)))
            }
        }
    }

    fun verify2faCode(code: String) {
        val token = tempTwoFactorToken ?: run {
            _finalLoginResult.postValue(Result.failure(Exception("2FA token is missing.")))
            return
        }

        viewModelScope.launch {
            try {
                val request = Verify2faRequest(twoFactorToken = token, code = code)
                val response = RetrofitClient.authApiService.verify2fa(request)

                _finalLoginResult.postValue(Result.success(response.token))
                _uiState.value = LoginUiState.EnteringCredentials

            } catch (e: Exception) {
                // Applying similar smart error handling here
                val errorMessage = when (e) {
                    is HttpException -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                            "Verification Failed: ${apiError.errorCode}"
                        } catch (jsonError: Exception) {
                            "Invalid 2FA code."
                        }
                    }
                    is IOException -> "Could not connect to the server. Please check your network."
                    else -> e.message ?: "An unknown error occurred."
                }
                _finalLoginResult.postValue(Result.failure(Exception(errorMessage)))
            }
        }
    }
}

