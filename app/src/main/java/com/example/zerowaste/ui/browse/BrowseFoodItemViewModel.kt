package com.example.zerowaste.ui.browse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.data.remote.BrowseFoodItemResponse
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
import java.text.SimpleDateFormat
import java.util.*

// 1. Added 'name' to the filters
data class BrowseFilters(
    val isInventoryOnly: Boolean = false,
    val isDonationsOnly: Boolean = false,
    val name: String? = null,
    val category: String? = null,
    val storageLocation: String? = null,
    val expiryDate: String? = null,
    val sort: List<String> = listOf("expiryDate")
)

data class BrowseFoodUiState(
    val items: List<BrowseFoodItemResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val endReached: Boolean = false,
    val currentPage: Int = 1
)

class BrowseFoodItemViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getBrowseFoodApi(application)
    private val sessionManager = SessionManager(application)

    private val notificationApiService: NotificationApiService = RetrofitClient.getNotificationApi(application)
    private val _uiState = MutableStateFlow(BrowseFoodUiState())
    val uiState: StateFlow<BrowseFoodUiState> = _uiState

    private val _filters = MutableStateFlow(BrowseFilters())
    val filters: StateFlow<BrowseFilters> = _filters

    fun claimDonation(item: BrowseFoodItemResponse) {
        viewModelScope.launch {
            try {
                // Step A: Get data for notification
                val userId = sessionManager.getUserId() ?: 1L

                // Step B: Create the DONATION_CLAIMED notification
                val notificationDTO = createDonationClaimedDTO(item, userId)
                val notificationResponse = notificationApiService.createNotification(notificationDTO)
                if (!notificationResponse.isSuccessful) throw IOException("Failed to create notification")

                // Step C: Refresh the list to remove the claimed item
                loadItems(isFirstLoad = true)

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // --- HELPER FUNCTION: Create Notification DTO for Claiming ---
    private fun createDonationClaimedDTO(item: BrowseFoodItemResponse, userId: Long): NotificationReqDTO {
        return NotificationReqDTO(
            notifType = NotificationType.DONATION_CLAIMED,
            usersId = userId,
            itemName = listOf(item.itemName),
            quantity = listOf(item.quantity.toLong()),
            expiryDate = null,
            meal = null
        )
    }


    // --- FILTER HANDLERS ---
    fun onNameSearch(query: String) {
        _filters.value = _filters.value.copy(name = query.ifBlank { null })
        loadItems(isFirstLoad = true)
    }

    fun onCategorySearch(query: String) {
        _filters.value = _filters.value.copy(category = query.ifBlank { null })
        loadItems(isFirstLoad = true)
    }

    fun onStorageLocationSearch(query: String) {
        _filters.value = _filters.value.copy(storageLocation = query.ifBlank { null })
        loadItems(isFirstLoad = true)
    }

    fun onExpiryDateSelected(dateMillis: Long?) {
        val formattedDate = dateMillis?.let { formatMillisToDateString(it) }
        _filters.value = _filters.value.copy(expiryDate = formattedDate)
        loadItems(isFirstLoad = true)
    }

    fun onInventoryOnlyToggled(isSelected: Boolean) {
        _filters.value = _filters.value.copy(isInventoryOnly = isSelected, isDonationsOnly = false)
        loadItems(isFirstLoad = true)
    }
    fun onDonationsOnlyToggled(isSelected: Boolean) {
        _filters.value = _filters.value.copy(isDonationsOnly = isSelected, isInventoryOnly = false)
        loadItems(isFirstLoad = true)
    }
    fun onSortChanged(sortOption: String) {
        _filters.value = _filters.value.copy(sort = listOf(sortOption))
        loadItems(isFirstLoad = true)
    }
    fun loadMoreItems() {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || _uiState.value.endReached) return
        loadItems()
    }

    /**
     * Loads food items, applying filtering logic to control visibility based on ownership and donation status.
     */
    fun loadItems(isFirstLoad: Boolean = false) {
        viewModelScope.launch {
            val pageToLoad = if (isFirstLoad) 1 else _uiState.value.currentPage + 1
            _uiState.value = _uiState.value.copy(isLoading = isFirstLoad, isLoadingMore = !isFirstLoad, error = null)
            try {
                val currentFilters = _filters.value
                val userId = sessionManager.getUserId()

                // ⭐ START OF FINALIZED FILTERING LOGIC ⭐
                val requestUserId: Long?
                val requestConvertToDonation: Boolean?

                if (currentFilters.isDonationsOnly) {
                    // Scenario 1: Donations Only
                    requestUserId = null // DO NOT filter by user ID (show all)
                    requestConvertToDonation = true // Filter strictly by donation flag
                } else if (currentFilters.isInventoryOnly) {
                    // Scenario 2: Inventory Only
                    requestUserId = userId // Filter by current user ID
                    requestConvertToDonation = false // Filter out items flagged as donation
                } else {
                    // Scenario 3 (Default/No filters active): Show MY Inventory AND All Donations
                    requestUserId = userId
                    requestConvertToDonation = null
                }

                // ⭐ END OF FINALIZED FILTERING LOGIC ⭐

                val response = apiService.getBrowseList(
                    page = pageToLoad,
                    pageSize = 10,
                    usersId = requestUserId,
                    convertToDonation = requestConvertToDonation,
                    name = currentFilters.name,
                    category = currentFilters.category,
                    storageLocation = currentFilters.storageLocation,
                    expiryDate = currentFilters.expiryDate,
                    sort = currentFilters.sort
                ).data

                _uiState.value = _uiState.value.copy(
                    items = if (isFirstLoad) response.content else _uiState.value.items + response.content,
                    currentPage = pageToLoad,
                    endReached = response.last,
                    isLoading = false,
                    isLoadingMore = false
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "An unexpected error occurred: ${e.code()}"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "Couldn't reach server. Check your internet connection."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = "An unknown error occurred."
                )
            }
        }
    }

    private fun formatMillisToDateString(millis: Long): String {
        val date = Date(millis)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }
}