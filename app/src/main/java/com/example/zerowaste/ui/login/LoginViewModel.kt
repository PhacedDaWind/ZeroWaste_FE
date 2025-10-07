package com.example.zerowaste.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.LoginRequest
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    // LiveData to hold the login result (token or error message)
    private val _loginResult = MutableLiveData<Result<String>>()
    val loginResult: LiveData<Result<String>> = _loginResult

    fun login(username: String, password: String) {
        // Use viewModelScope to launch a coroutine
        viewModelScope.launch {
            try {
                val request = LoginRequest(username, password)
                val response = RetrofitClient.authApiService.login(request)
                // Post a success result with the token
                _loginResult.postValue(Result.success(response.token))
            } catch (e: Exception) {
                // Post a failure result with the error message
                _loginResult.postValue(Result.failure(e))
            }
        }
    }
}