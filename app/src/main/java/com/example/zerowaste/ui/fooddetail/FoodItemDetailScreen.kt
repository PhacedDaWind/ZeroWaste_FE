package com.example.zerowaste.ui.fooddetail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zerowaste.data.remote.FoodItemDetailResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemDetailScreen(
    itemId: Long,
    viewModel: FoodItemDetailViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // This effect triggers the data loading when the screen is first displayed
    LaunchedEffect(itemId) {
        viewModel.loadItemDetails(itemId)
    }

    // This effect shows a toast message on successful update and then resets the flag
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            Toast.makeText(context, "Action type updated successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccessFlag()
        }
    }

    // --- NEW: This effect shows a toast for any errors ---
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            // Optional: You can add a function to the ViewModel to clear the error after showing it.
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.item != null -> {
                    ItemDetailContent(
                        item = uiState.item!!,
                        isUpdating = uiState.isUpdating,
                        onActionTypeChange = { newActionType ->
                            viewModel.updateActionType(itemId, newActionType)
                        }
                    )
                }
                // Show a generic error message if loading fails
                else -> {
                    Text(
                        text = "Failed to load item details.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ItemDetailContent(
    item: FoodItemDetailResponse,
    isUpdating: Boolean,
    onActionTypeChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Divider()
        DetailRow(label = "Category", value = item.category ?: "N/A")
        DetailRow(label = "Quantity", value = item.quantity.toString())
        DetailRow(label = "Expiry Date", value = item.expiryDate ?: "N/A")
        DetailRow(label = "Storage Location", value = item.storageLocation ?: "N/A")
        DetailRow(label = "Pickup Location", value = item.pickupLocation ?: "N/A")
        DetailRow(label = "Contact Method", value = item.contactMethod ?: "N/A")
        DetailRow(label = "Remarks", value = item.remarks ?: "None")
        Divider()

        // Action Type Selector (Now updated)
        ActionTypeSelector(
            currentActionType = item.actionType,
            isUpdating = isUpdating,
            onActionSelected = onActionTypeChange
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// --- UPDATED: This composable now handles displaying labels ---
@Composable
fun ActionTypeSelector(
    currentActionType: String,
    isUpdating: Boolean,
    onActionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // 1. Map of enum values to user-friendly labels
    val actionTypeLabels = mapOf(
        "MARK_AS_USED" to "Mark as used",
        "PLAN_FOR_MEAL" to "Plan for meal",
        "FLAG_FOR_DONATION" to "Flag for donation"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Action:", style = MaterialTheme.typography.titleMedium)
        if (isUpdating) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    // 2. Display the label for the current action type
                    Text(actionTypeLabels[currentActionType] ?: currentActionType)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // 3. Iterate over the map to create menu items
                    actionTypeLabels.forEach { (enumValue, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onActionSelected(enumValue) // Send the raw enum value to the API
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

