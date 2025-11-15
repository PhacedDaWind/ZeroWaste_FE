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

    /**
     * Loads an existing item's details.
     * If itemId is null, we're in "Create" mode, so we just set loading to false.
     */
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
        actionType: String,
        convertToDonation: Boolean,
        reservedQuantity: Long?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "User not logged in.")
                return@launch
            }

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
                actionType = actionType,
                userId = userId,
                convertToDonation = convertToDonation,
                reservedQuantity = reservedQuantity
            )

            try {
                if (itemId == null) {
                    // Create new item
                    inventoryApiService.createFoodItem(request)
                } else {
                    // Update existing item
                    inventoryApiService.updateFoodItem(itemId, request)
                }
                _uiState.value = _uiState.value.copy(isLoading = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to save item.")
            }
        }
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