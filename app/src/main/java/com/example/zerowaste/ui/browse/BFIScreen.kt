package com.example.zerowaste.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.zerowaste.viewmodel.BrowseFoodItemViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseFoodItemScreen(
    userId: Long,
    viewModel: BrowseFoodItemViewModel = viewModel()
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBrowseFoodItems(context, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Browse Food Items") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> Text("Error: $error", color = MaterialTheme.colorScheme.error)
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(items) { food ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Category: ${food.category ?: "N/A"}")
                                Text("Quantity: ${food.quantity ?: 0}")
                                Text("Location: ${food.pickupLocation ?: "N/A"}")
                                Text("Expiry: ${food.expiryDate ?: "N/A"}")
                                Text("Posted by: ${food.userName ?: "Unknown"}")
                            }
                        }
                    }
                }
            }
        }
    }
}
