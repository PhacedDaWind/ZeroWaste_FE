package com.example.zerowaste.ui.registration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.ApiErrorResponse
import com.example.zerowaste.data.remote.RegistrationRequest
import com.example.zerowaste.data.remote.RegistrationResponse
import com.example.zerowaste.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getInstance(application)
    private val _registrationResult = MutableLiveData<Result<RegistrationResponse>>()
    val registrationResult: LiveData<Result<RegistrationResponse>> = _registrationResult

    fun register(request: RegistrationRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.registerUser(request)
                // The 'data' field from your ApiResponse wrapper contains the user info
                _registrationResult.postValue(Result.success(response.data))
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> {
                        try {
                            val errorBody = e.response()?.errorBody()?.string()
                            val apiError = Gson().fromJson(errorBody, ApiErrorResponse::class.java)
                            "Registration Failed: ${apiError.errorCode}"
                        } catch (jsonError: Exception) {
                            "An unexpected error occurred during registration."
                        }
                    }
                    is IOException -> "Could not connect to the server."
                    else -> e.message ?: "An unknown registration error occurred."
                }
                _registrationResult.postValue(Result.failure(Exception(errorMessage)))
            }
        }
    }
}
