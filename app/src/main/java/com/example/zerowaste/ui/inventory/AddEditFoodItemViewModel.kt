package com.example.zerowaste.ui.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.FoodItemRequest
import com.example.zerowaste.data.remote.FoodItemResponse
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// Defines the state for the Add/Edit screen
data class AddEditUiState(
    val item: FoodItemResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false // Flag to trigger navigation back
)

class AddEditFoodItemViewModel(application: Application) : AndroidViewModel(application) {

    private val inventoryApiService = RetrofitClient.getFoodInventoryApi(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    fun loadItem(itemId: Long?) {
        if (itemId == null) {
            _uiState.value = AddEditUiState(isLoading = false) // Ready for "Create"
            return
        }

        viewModelScope.launch {
            _uiState.value = AddEditUiState(isLoading = true)
            try {
                val response = inventoryApiService.getFoodItem(itemId)
                _uiState.value = AddEditUiState(
                    item = response.data,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = AddEditUiState(error = "Failed to load item.")
            }
        }
    }

    /**
     * Saves the item (either creates a new one or updates an existing one).
     */
    fun saveItem(
        itemId: Long?, // Null if creating a new item
        name: String,
        quantity: Long,
        expiryDate: String?, // "YYYY-MM-DD"
        category: String?,
        storageLocation: String?,
        remarks: String?,
        contactMethod: String?,
        pickupLocation: String?,
        // actionType is removed from parameters
        convertToDonation: Boolean,
        reservedQuantity: Long
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // --- 1. ADDED: Reserved Quantity Validation ---
            if (reservedQuantity > quantity) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Reserved quantity cannot be greater than total quantity."
                )
                return@launch
            }

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in.")
                return@launch
            }

            // --- 2. UPDATED: Get actionType from the loaded item or set a default ---
            val actionType = _uiState.value.item?.actionType ?: "PLAN_FOR_MEAL" // Default for new items

            // Create the request object
            val request = FoodItemRequest(
                name = name,
                quantity = quantity,
                expiryDate = expiryDate,
                category = category,
                storageLocation = storageLocation,
                remarks = remarks,
                contactMethod = contactMethod,
                pickupLocation = pickupLocation,
                actionType = actionType, // Use the determined actionType
                userId = userId,
                convertToDonation = convertToDonation,
                reservedQuantity = reservedQuantity
            )

            try {
                if (itemId == null) {
                    inventoryApiService.createFoodItem(request)
                } else {
                    inventoryApiService.updateFoodItem(itemId, request)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to save item.")
            }
        }
    }

    // Function to clear the error message after it has been shown
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // Helper to format date from DatePicker (Long) to String (YYYY-MM-DD)
    fun formatMillisToDateString(millis: Long): String {
        val date = Date(millis)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }

    // Helper to parse date String (YYYY-MM-DD) to Long for the DatePicker
    fun parseDateStringToMillis(dateStr: String?): Long? {
        if (dateStr == null) return null
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            formatter.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}