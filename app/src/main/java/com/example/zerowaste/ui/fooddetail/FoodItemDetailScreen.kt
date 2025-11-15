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
    isInventory: Boolean, // ⭐ --- ADDED ---
    viewModel: FoodItemDetailViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 1. Load data when the screen appears
    LaunchedEffect(itemId) {
        viewModel.loadItemDetails(itemId)
    }

    // 2. Show a "Success" toast
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            Toast.makeText(context, "Action type updated successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccessFlag()
        }
    }

    // 3. Show an error toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            // You can add viewModel.clearError() here if you implement it
        }
    }

    // 4. --- THIS IS THE FIX for the "stuck action" bug ---
    // Resets the ViewModel's state when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
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
                            // This calls the ViewModel function you provided
                            viewModel.updateActionType(itemId, newActionType)
                        },
                        // ⭐ --- MODIFIED: Hide actions if isInventory is true ---
                        showActions = !isInventory
                    )
                }
                else -> {
                    // Show error or empty state
                    Text(
                        text = uiState.error ?: "Failed to load item details.",
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
    onActionTypeChange: (String?) -> Unit, // Accepts nullable String
    showActions: Boolean // ⭐ --- ADDED ---
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
        HorizontalDivider()
        DetailRow(label = "Category", value = item.category ?: "N/A")
        DetailRow(label = "Quantity", value = item.quantity.toString())
        DetailRow(label = "Expiry Date", value = item.expiryDate ?: "N/A")
        DetailRow(label = "Storage Location", value = item.storageLocation ?: "N/A")
        DetailRow(label = "Pickup Location", value = item.pickupLocation ?: "N/A")
        DetailRow(label = "Contact Method", value = item.contactMethod ?: "N/A")
        DetailRow(label = "Remarks", value = item.remarks ?: "None")
        HorizontalDivider()

        // ⭐ --- MODIFIED: Conditionally show the action selector ---
        if (showActions) {
            ActionTypeSelector(
                currentActionType = item.actionType,
                isUpdating = isUpdating,
                onActionSelected = onActionTypeChange
            )
        }
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

@Composable
fun ActionTypeSelector(
    currentActionType: String?,
    isUpdating: Boolean,
    onActionSelected: (String?) -> Unit // Accepts nullable String
) {
    var expanded by remember { mutableStateOf(false) }

    // Fixes the "menu stuck open" bug
    DisposableEffect(Unit) {
        onDispose {
            expanded = false
        }
    }

    // List of options, including 'null'
    val actionTypeOptions = listOf(
        null to "...", // Your null option
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
            // --- FIX: Box wrapper fixes visual clipping bug ---
            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    // Find the label for the current action type
                    val label = actionTypeOptions.find { it.first == currentActionType }?.second ?: "..."
                    Text(label)
                }

                // The DropdownMenu is INSIDE the Box
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    actionTypeOptions.forEach { (actionValue, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onActionSelected(actionValue) // Sends null or the string
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}