package com.example.zerowaste.ui.inventory

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zerowaste.data.remote.BrowseFoodItemResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodInventoryScreen(
    viewModel: FoodInventoryViewModel,
    navController: NavController,
    onNavigateToAddItem: () -> Unit, // Callback to navigate to Add screen
    onNavigateToEditItem: (Long) -> Unit // Callback to navigate to Edit screen
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Trigger data loading when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadInventoryItems()
    }

    // Show a toast for any errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Food Inventory") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddItem) {
                Icon(Icons.Default.Add, contentDescription = "Add Food Item")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.items.isEmpty()) {
                Text(
                    text = "Your inventory is empty. Tap the '+' button to add an item.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items) { item ->
                        InventoryItemCard(
                            item = item,
                            onDelete = { viewModel.deleteItem(item.id) },
                            onEdit = { onNavigateToEditItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

// --- THIS IS THE UPDATED CARD ---
@Composable
fun InventoryItemCard(
    item: BrowseFoodItemResponse,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onEdit // Make the whole card clickable to edit
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {

                // 1. Header Row with Name and Donation Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        // Allow text to wrap if badge is present
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // --- THE NEW INDICATOR ---
                    if (item.convertToDonation) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Donation",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 2. Item details
                Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Text("Expires: ${item.expiryDate ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}