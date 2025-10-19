package com.example.zerowaste.ui.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zerowaste.data.remote.BrowseFoodItemResponse
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseFoodItemScreen(
    viewModel: BrowseFoodItemViewModel,
    appNavController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val listState = rememberLazyListState()

    // Load initial items
    LaunchedEffect(Unit) {
        viewModel.loadItems(isFirstLoad = true)
    }

    Scaffold(
        topBar = {
            FilterBar(
                filters = filters,
                onInventoryToggled = { viewModel.onInventoryOnlyToggled(it) },
                onDonationsToggled = { viewModel.onDonationsOnlyToggled(it) },
                onDateSelected = { viewModel.onExpiryDateSelected(it) },
                onSortChanged = { viewModel.onSortChanged(it) },
                onNameSearch = { viewModel.onNameSearch(it) },
                onCategorySearch = { viewModel.onCategorySearch(it) },
                onStorageSearch = { viewModel.onStorageLocationSearch(it) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (uiState.items.isEmpty()) {
                // --- ADDED: Empty state message ---
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No items found. Try adjusting your filters.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.id }) { item ->
                        FoodItemCard(
                            item = item,
                            onClick = {
                                appNavController.navigate("food_detail/${item.id}")
                            }
                        )
                    }

                    item {
                        if (uiState.isLoadingMore) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                // Infinite scroll listener
                LaunchedEffect(listState) {
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .collect { lastVisibleItemIndex ->
                            if (lastVisibleItemIndex != null && lastVisibleItemIndex >= uiState.items.size - 5 && !uiState.isLoadingMore && !uiState.endReached) {
                                viewModel.loadMoreItems()
                            }
                        }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    filters: BrowseFilters,
    onInventoryToggled: (Boolean) -> Unit,
    onDonationsToggled: (Boolean) -> Unit,
    onDateSelected: (Long?) -> Unit,
    onSortChanged: (String) -> Unit,
    onNameSearch: (String) -> Unit,
    onCategorySearch: (String) -> Unit,
    onStorageSearch: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // State for the text fields
    var nameQuery by remember { mutableStateOf(filters.name ?: "") }
    var categoryQuery by remember { mutableStateOf(filters.category ?: "") }
    var storageQuery by remember { mutableStateOf(filters.storageLocation ?: "") }

    // --- MODIFIED: Debouncing logic for automatic search ---
    LaunchedEffect(nameQuery) {
        delay(500L) // Wait for 500ms after user stops typing
        if (nameQuery != filters.name) {
            onNameSearch(nameQuery)
        }
    }
    LaunchedEffect(categoryQuery) {
        delay(500L)
        if (categoryQuery != filters.category) {
            onCategorySearch(categoryQuery)
        }
    }
    LaunchedEffect(storageQuery) {
        delay(500L)
        if (storageQuery != filters.storageLocation) {
            onStorageSearch(storageQuery)
        }
    }

    Column {
        TopAppBar(
            title = { Text("Browse Items") },
            actions = {
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort Options")
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Earliest Expiry") },
                            onClick = {
                                onSortChanged("expiryDate")
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Latest Expiry") },
                            onClick = {
                                onSortChanged("-expiryDate")
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = filters.isInventoryOnly,
                onClick = { onInventoryToggled(!filters.isInventoryOnly) },
                label = { Text("My Inventory") }
            )
            FilterChip(
                selected = filters.isDonationsOnly,
                onClick = { onDonationsToggled(!filters.isDonationsOnly) },
                label = { Text("Donations") }
            )
            AssistChip(
                onClick = { showDatePicker = true },
                label = { Text(filters.expiryDate ?: "Expiry Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Select Date") }
            )
        }

        // --- MODIFIED: Search field for Name with automatic search ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nameQuery,
                onValueChange = { nameQuery = it },
                label = { Text("Search by Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (nameQuery.isNotEmpty()) {
                        IconButton(onClick = { nameQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )
        }

        // --- MODIFIED: Search fields with automatic search ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = categoryQuery,
                onValueChange = { categoryQuery = it },
                label = { Text("Category") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                trailingIcon = {
                    if (categoryQuery.isNotEmpty()) {
                        IconButton(onClick = { categoryQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear category")
                        }
                    }
                }
            )
            OutlinedTextField(
                value = storageQuery,
                onValueChange = { storageQuery = it },
                label = { Text("Storage") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                trailingIcon = {
                    if (storageQuery.isNotEmpty()) {
                        IconButton(onClick = { storageQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear storage")
                        }
                    }
                }
            )
        }
    }

    // --- MODIFIED: Date Picker Dialog with a Clear button ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onDateSelected(null) // Clear the date
                            showDatePicker = false
                        }
                    ) { Text("Clear") }
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun FoodItemCard(
    item: BrowseFoodItemResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = item.itemName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Quantity: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                Text("Expires: ${item.expiryDate ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            }
            Divider()
            Text(
                "Pickup Location: ${item.pickupLocation ?: "Not specified"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Contact Method: ${item.contactMethod ?: "Not specified"}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

