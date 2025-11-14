package com.example.zerowaste.ui.fooddetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.FoodItemDetailResponse
import com.example.zerowaste.data.remote.NotificationApiService
import com.example.zerowaste.data.remote.NotificationReqDTO
import com.example.zerowaste.data.remote.NotificationType
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.*

// --- Assume FoodItemDetailResponse and FoodItemApiService are defined in your data package ---

data class FoodItemDetailUiState(
    val item: FoodItemDetailResponse? = null,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false
)

class FoodItemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getBrowseFoodApi(application)
    private val notificationApiService: NotificationApiService = RetrofitClient.getNotificationApi(application)
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(FoodItemDetailUiState())
    val uiState: StateFlow<FoodItemDetailUiState> = _uiState

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

    fun updateActionType(itemId: Long, newActionType: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            try {
                // 1. Call your existing apiService function to update the item
                val convertToDonationFlag = (newActionType == null)

                apiService.updateFoodItemActionType(
                    itemId = itemId,
                    convertToDonation = convertToDonationFlag,
                    actionType = newActionType
                )

                // 2. Trigger Notification if an action was selected
                val userId = sessionManager.getUserId() ?: 1L
                val item = _uiState.value.item
                if (item == null) throw IllegalStateException("No item details loaded")

                if (newActionType != null) {
                    postNotification(item, userId)
                }

                // 3. Update local UI state on success
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateSuccess = true,
                        item = _uiState.value.item?.copy(actionType = newActionType)
                    )
                }
            } catch (e: Exception) {
                Log.e("FoodItemDetailVM", "Error updating action type", e)
                val errorMessage = when (e) {
                    is HttpException -> "Update failed: HTTP ${e.code()}. Check backend logs."
                    is IOException -> "Network error. Please check your connection."
                    else -> "An unexpected error occurred during update."
                }
                _uiState.update { it.copy(isUpdating = false, error = errorMessage) }
            }
        }
    }

    // --- Core logic: Enforce DONATION_CLAIMED type for ANY action ---
    private suspend fun postNotification(item: FoodItemDetailResponse, userId: Long) {
        val notificationDTO = createDonationClaimedDTO(item, userId)

        try {
            // POST the notification to the API
            val notificationResponse = notificationApiService.createNotification(notificationDTO)
            if (!notificationResponse.isSuccessful) {
                Log.e("FoodItemDetailVM", "Failed to create notification: ${notificationResponse.code()}")
            }
        } catch (e: Exception) {
            Log.e("FoodItemDetailVM", "Network error during notification POST: ${e.message}")
        }
    }

    // Helper function to build the DTO (Passes data, enforces type)
    private fun createDonationClaimedDTO(item: FoodItemDetailResponse, userId: Long): NotificationReqDTO {
        return NotificationReqDTO(
            notifType = NotificationType.DONATION_CLAIMED,
            usersId = userId,
            itemName = listOf(item.name),
            quantity = listOf(item.quantity.toLong()),
            expiryDate = null,
            meal = null
        )
    }

    fun resetUpdateSuccessFlag() {
        _uiState.update { it.copy(updateSuccess = false) }
    }

    fun resetState() {
        _uiState.update { FoodItemDetailUiState() }
    }
}