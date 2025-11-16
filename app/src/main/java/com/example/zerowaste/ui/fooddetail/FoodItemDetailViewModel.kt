package com.example.zerowaste.ui.fooddetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.FoodItemDetailResponse
import com.example.zerowaste.data.remote.RetrofitClient
// Import the new UserInfo class
import com.example.zerowaste.data.remote.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.*

data class FoodItemDetailUiState(
    val item: FoodItemDetailResponse? = null,
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false,
    val isOwner: Boolean = false // ADDED: To track if the logged-in user owns this item
)

class FoodItemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getBrowseFoodApi(application)
    // REMOVED: private val notificationApiService
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(FoodItemDetailUiState())
    val uiState: StateFlow<FoodItemDetailUiState> = _uiState

    fun loadItemDetails(itemId: Long) {
        viewModelScope.launch {
            _uiState.value = FoodItemDetailUiState(isLoading = true)
            // 1. Get current user's ID
            val currentUserId = sessionManager.getUserId() ?: -1L

            try {
                val response = apiService.getFoodItemDetail(itemId)

                // 2. Check if the current user is the owner
                val isOwner = response.data?.user?.id == currentUserId

                _uiState.value = FoodItemDetailUiState(
                    item = response.data,
                    isLoading = false,
                    isOwner = isOwner // 3. Set the state
                )
            } catch (e: Exception) {
                _uiState.value = FoodItemDetailUiState(
                    isLoading = false,
                    error = "Failed to load item details.",
                    isOwner = false
                )
            }
        }
    }

    fun updateActionType(itemId: Long, newActionType: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            try {
                // Get the ID of the logged-in user (the one claiming the item)
                val claimantUserId = sessionManager.getUserId() ?: 1L

                // ⭐ REMOVED: val claimantUsername = sessionManager.getUsername() ...

                val convertToDonationFlag = (newActionType == null)

                // Call the API to update the item
                // The backend will now handle creating the notification
                apiService.updateFoodItemActionType(
                    itemId = itemId,
                    convertToDonation = convertToDonationFlag,
                    actionType = newActionType,
                    claimantUserId = claimantUserId
                )

                // REMOVED: All notification logic (postNotification call) is gone.

                // Update local UI state on success
                _uiState.update {
                    it.copy(
                        isUpdating = false,
                        updateSuccess = true,
                        item = _uiState.value.item?.copy(
                            actionType = newActionType,
                            user = UserInfo( // Optimistically update owner
                                id = claimantUserId,
                                // ⭐ MODIFIED: Reuse the original username as a placeholder
                                username = _uiState.value.item!!.user.username
                            )
                        ),
                        // ADDED: Once claimed, the claimant becomes the new owner
                        isOwner = true
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

    // REMOVED: The postNotification and createDonationClaimedDTO functions are gone.

    fun resetUpdateSuccessFlag() {
        _uiState.update { it.copy(updateSuccess = false) }
    }

    fun resetState() {
        _uiState.update { FoodItemDetailUiState() }
    }
}