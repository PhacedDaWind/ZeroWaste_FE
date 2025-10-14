package com.example.zerowaste.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerowaste.data.remote.BrowseFoodItemResponse
import com.example.zerowaste.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BrowseFoodItemViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<BrowseFoodItemResponse>>(emptyList())
    val items: StateFlow<List<BrowseFoodItemResponse>> = _items

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadBrowseFoodItems(context: android.content.Context, userId: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val api = RetrofitClient.getBrowseFoodApi(context) as com.example.zerowaste.data.remote.BrowseFoodItemApiService
                val response = api.getBrowseFoodItems(usersId = userId)

                if (response.isSuccessful && response.body() != null) {
                    _items.value = response.body()!!.content
                } else {
                    _error.value = "Failed to load food items (${response.code()})"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
