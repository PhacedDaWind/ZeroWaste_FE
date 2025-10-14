package com.example.zerowaste.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.ExpiringItem
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class HomeUiState(
    val username: String = "",
    val totalItems: Int = 0,
    val donationsMade: Int = 0,
    val expiringItems: List<ExpiringItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    // The init block is now removed.

    // 1. Renamed to a public function that the screen can call
    fun loadHomeScreenData() {
        // Only fetch if data is not already loaded or is stale.
        // For simplicity now, we'll just re-fetch every time.
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = HomeUiState(isLoading = false, errorMessage = "User not logged in.")
                return@launch
            }

            try {
                val response = apiService.getUserDetails(userId)
                val userDetails = response.data

                val fetchedExpiringItems = listOf(
                    ExpiringItem("Milk", "1 Litre", "3 days"),
                    ExpiringItem("Chicken Breast", "500g", "1 day"),
                    ExpiringItem("Lettuce", "1 head", "4 days")
                )

                _uiState.value = HomeUiState(
                    username = userDetails.username,
                    totalItems = userDetails.totalItems.toInt(),
                    donationsMade = userDetails.donationsMade.toInt(),
                    expiringItems = fetchedExpiringItems,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Error fetching data from server: HTTP ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred."
                }
                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
}

