package com.example.zerowaste.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This data class will hold all the state for the Home screen
data class HomeUiState(
    val username: String = "",
    val totalItems: Int = 0,
    val donationsMade: Int = 0,
    val expiringItems: List<ExpiringItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadHomeScreenData()
    }

    private fun loadHomeScreenData() {
        viewModelScope.launch {
            try {
                // In a real app, you would make your API calls here.
                // We'll simulate a network delay.
                delay(1500)

                // --- Placeholder Data ---
                val fetchedUsername = "Tommy"
                val fetchedTotalItems = 15
                val fetchedDonationsMade = 2
                val fetchedExpiringItems = listOf(
                    ExpiringItem("Milk", "1 Litre", "3 days"),
                    ExpiringItem("Chicken Breast", "500g", "1 day"),
                    ExpiringItem("Lettuce", "1 head", "4 days")
                )

                // Update the UI state with the fetched data
                _uiState.value = HomeUiState(
                    username = fetchedUsername,
                    totalItems = fetchedTotalItems,
                    donationsMade = fetchedDonationsMade,
                    expiringItems = fetchedExpiringItems,
                    isLoading = false
                )

            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    errorMessage = "Failed to load data. Please try again."
                )
            }
        }
    }
}
