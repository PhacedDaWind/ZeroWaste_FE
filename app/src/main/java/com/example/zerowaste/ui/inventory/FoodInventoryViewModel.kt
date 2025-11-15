package com.example.zerowaste.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.BrowseFoodItemResponse
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class FoodInventoryUiState(
    val items: List<BrowseFoodItemResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FoodInventoryViewModel(application: Application) : AndroidViewModel(application) {

    // We use both API services:
    // 1. The Browse API to get the user's list
    private val browseApiService = RetrofitClient.getBrowseFoodApi(application)
    // 2. The Inventory API to delete items
    private val inventoryApiService = RetrofitClient.getFoodInventoryApi(application)

    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(FoodInventoryUiState())
    val uiState: StateFlow<FoodInventoryUiState> = _uiState

    fun loadInventoryItems() {
        viewModelScope.launch {
            _uiState.value = FoodInventoryUiState(isLoading = true)

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = FoodInventoryUiState(error = "User not logged in.")
                return@launch
            }

            try {
                // Call the browse list API, but force the usersId filter
                val response = browseApiService.getBrowseList(
                    page = 1,
                    pageSize = 100, // Get all items for now, or implement pagination
                    usersId = userId // This is the key: only get this user's items
                ).data

                _uiState.value = FoodInventoryUiState(
                    items = response.content,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> "Error fetching inventory: HTTP ${e.code()}"
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred."
                }
                _uiState.value = FoodInventoryUiState(error = errorMessage)
            }
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true) // Show loading
            try {
                // Call the new delete API
                inventoryApiService.deleteFoodItem(itemId)

                // On success, refresh the list by removing the item locally
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = _uiState.value.items.filterNot { it.id == itemId }
                )
            } catch (e: Exception) {
                // On failure, stop loading and set an error
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete item."
                )
            }
        }
    }
}