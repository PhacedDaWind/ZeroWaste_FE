package com.example.zerowaste.ui.browse

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
import java.text.SimpleDateFormat
import java.util.*

// 1. The expiryDate is now a String? to hold the formatted date "YYYY-MM-DD"
data class BrowseFilters(
    val isInventoryOnly: Boolean = false,
    val isDonationsOnly: Boolean = false,
    val category: String? = null,
    val storageLocation: String? = null,
    val expiryDate: String? = null, // Changed from LocalDate? to String?
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

    private val _uiState = MutableStateFlow(BrowseFoodUiState())
    val uiState: StateFlow<BrowseFoodUiState> = _uiState

    private val _filters = MutableStateFlow(BrowseFilters())
    val filters: StateFlow<BrowseFilters> = _filters


    // --- NEW FUNCTIONS FOR CATEGORY AND STORAGE ---
    fun onCategorySearch(query: String) {
        _filters.value = _filters.value.copy(category = query.ifBlank { null })
        loadItems(isFirstLoad = true)
    }

    fun onStorageLocationSearch(query: String) {
        _filters.value = _filters.value.copy(storageLocation = query.ifBlank { null })
        loadItems(isFirstLoad = true)
    }
    // 2. This function now accepts the raw Long timestamp
    fun onExpiryDateSelected(dateMillis: Long?) {
        // Format the date here in the ViewModel
        val formattedDate = dateMillis?.let { formatMillisToDateString(it) }
        _filters.value = _filters.value.copy(expiryDate = formattedDate)
        loadItems(isFirstLoad = true)
    }

    // --- Other filter functions remain the same ---
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

    fun loadItems(isFirstLoad: Boolean = false) {
        viewModelScope.launch {
            val pageToLoad = if (isFirstLoad) 1 else _uiState.value.currentPage + 1
            _uiState.value = _uiState.value.copy(isLoading = isFirstLoad, isLoadingMore = !isFirstLoad, error = null)
            try {
                val currentFilters = _filters.value
                val userId = sessionManager.getUserId()
                val response = apiService.getBrowseList(
                    page = pageToLoad,
                    pageSize = 10,
                    usersId = if (currentFilters.isInventoryOnly) userId else null,
                    convertToDonation = if (currentFilters.isDonationsOnly) true else null,
                    category = currentFilters.category,
                    storageLocation = currentFilters.storageLocation,
                    expiryDate = currentFilters.expiryDate, // Pass the formatted string
                    sort = currentFilters.sort
                ).data
                _uiState.value = _uiState.value.copy(
                    items = if (isFirstLoad) response.content else _uiState.value.items + response.content,
                    currentPage = pageToLoad,
                    endReached = response.last,
                    isLoading = false,
                    isLoadingMore = false
                )
            } catch (e: Exception) {
                // ... (error handling remains the same)
            }
        }
    }

    // 3. NEW private helper function to do the safe date conversion
    private fun formatMillisToDateString(millis: Long): String {
        val date = Date(millis)
        // SimpleDateFormat works on all API levels
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Adjust for timezone to prevent off-by-one day errors
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }
}

