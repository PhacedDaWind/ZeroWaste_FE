package com.example.zerowaste.ui.fooddetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.ApiErrorResponse
import com.example.zerowaste.data.remote.FoodItemDetailResponse
import com.example.zerowaste.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// This data class will hold all the state for the Food Item Detail screen
data class FoodItemDetailUiState(
    val item: FoodItemDetailResponse? = null,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false, // For showing a spinner during update
    val error: String? = null,
    val updateSuccess: Boolean = false
)

class FoodItemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getBrowseFoodApi(application)

    private val _uiState = MutableStateFlow(FoodItemDetailUiState())
    val uiState: StateFlow<FoodItemDetailUiState> = _uiState

    // Called by the UI to start loading the data
    fun loadItemDetails(itemId: Long) {
        viewModelScope.launch {
            _uiState.value = FoodItemDetailUiState(isLoading = true)
            try {
                val response = apiService.getFoodItemDetail(itemId)
                _uiState.value = FoodItemDetailUiState(
                    item = response.data,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = FoodItemDetailUiState(
                    isLoading = false,
                    error = "Failed to load item details."
                )
            }
        }
    }

    // Called by the UI when the user selects a new action type
    fun updateActionType(itemId: Long, newActionType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)
            try {
                apiService.updateFoodItemActionType(itemId, newActionType)

                // On success, update the local state to reflect the change immediately
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    updateSuccess = true,
                    item = _uiState.value.item?.copy(actionType = newActionType)
                )
            } catch (e: Exception) {
                // --- THIS BLOCK IS NOW MORE DETAILED ---
                Log.e("FoodItemDetailVM", "Error updating action type", e) // Log the full error
                val errorMessage = when (e) {
                    is HttpException -> "Update failed: HTTP ${e.code()}. Check backend logs."
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred during update."
                }
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = errorMessage
                )
            }
        }
    }

    // Called by the UI after the success message has been shown
    fun resetUpdateSuccessFlag() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}

