package com.example.zerowaste.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.ExpiringItemResponse // <-- IMPORT THIS
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Updated state to use ExpiringItemResponse
data class HomeUiState(
    val username: String = "",
    val totalItems: Int = 0,
    val donationsMade: Int = 0,
    val expiringItems: List<ExpiringItemResponse> = emptyList(), // <-- TYPE CHANGED HERE
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val authApiService = RetrofitClient.getAuthApi(application)
    private val inventoryApiService = RetrofitClient.getFoodInventoryApi(application) // Use the inventory API

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadHomeScreenData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = HomeUiState(isLoading = false, errorMessage = "User not logged in.")
                return@launch
            }

            try {
                // Run API calls in parallel
                coroutineScope {
                    val userDetailsDeferred = async { authApiService.getUserDetails(userId) }
                    val expiringItemsDeferred = async { inventoryApiService.getExpiringItems(userId) }

                    val userDetailsResponse = userDetailsDeferred.await()
                    val expiringItemsResponse = expiringItemsDeferred.await()

                    _uiState.value = HomeUiState(
                        username = userDetailsResponse.data.username,
                        totalItems = userDetailsResponse.data.totalItems.toInt(),
                        donationsMade = userDetailsResponse.data.donationsMade.toInt(),
                        // Use the real data from the API response
                        expiringItems = expiringItemsResponse.data,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Error fetching data: HTTP ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred: ${e.message}"
                }
                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
}