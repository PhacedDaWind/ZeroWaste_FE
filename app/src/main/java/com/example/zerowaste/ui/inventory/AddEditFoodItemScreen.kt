package com.example.zerowaste.ui.inventory

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFoodItemScreen(
    itemId: Long?, // Null if creating, non-null if editing
    viewModel: AddEditFoodItemViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Form state variables
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<String?>(null) } // "YYYY-MM-DD"
    var category by remember { mutableStateOf("") }
    var storageLocation by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var contactMethod by remember { mutableStateOf("") }
    var pickupLocation by remember { mutableStateOf("") }
    var convertToDonation by remember { mutableStateOf(false) }
    var reservedQuantity by remember { mutableStateOf("") }

    // State for validation errors
    var isNameError by remember { mutableStateOf(false) }
    var isQuantityError by remember { mutableStateOf(false) }
    var isReservedQtyError by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    // --- State Initialization ---

    LaunchedEffect(itemId) {
        viewModel.loadItem(itemId)
    }

    LaunchedEffect(uiState.item) {
        uiState.item?.let { item ->
            name = item.name
            quantity = item.quantity.toString()
            expiryDate = item.expiryDate
            category = item.category ?: ""
            storageLocation = item.storageLocation ?: ""
            remarks = item.remarks ?: ""
            contactMethod = item.contactMethod ?: ""
            pickupLocation = item.pickupLocation ?: ""
            convertToDonation = item.convertToDonation
            reservedQuantity = item.reservedQuantity.toString()
        }
    }

    // --- Navigation and Error Handling ---

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            Toast.makeText(context, "Item saved successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            // Clear the error in the ViewModel so it's only shown once
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (itemId == null) "Add New Item" else "Edit Item") },
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
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // --- The Form ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; isNameError = false },
                        label = { Text("Item Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = isNameError,
                        supportingText = { if (isNameError) Text("Name cannot be empty", color = MaterialTheme.colorScheme.error) },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) quantity = it
                            isQuantityError = false
                        },
                        label = { Text("Quantity*") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isQuantityError,
                        supportingText = { if (isQuantityError) Text("Quantity cannot be empty", color = MaterialTheme.colorScheme.error) }
                    )
                    OutlinedTextField(
                        value = expiryDate ?: "",
                        onValueChange = {},
                        label = { Text("Expiry Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = storageLocation,
                        onValueChange = { storageLocation = it },
                        label = { Text("Storage Location") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // --- ACTION TYPE SELECTOR HAS BEEN REMOVED ---

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Flag for Donation?", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = convertToDonation,
                            onCheckedChange = { convertToDonation = it }
                        )
                    }

                    // Fields visible only if "Flag for Donation" is true
                    if (convertToDonation) {
                        OutlinedTextField(
                            value = pickupLocation,
                            onValueChange = { pickupLocation = it },
                            label = { Text("Pickup Location") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = contactMethod,
                            onValueChange = { contactMethod = it },
                            label = { Text("Contact Method") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = reservedQuantity,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) reservedQuantity = it
                            isReservedQtyError = false
                        },
                        label = { Text("Reserved Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = isReservedQtyError,
                        supportingText = { if (isReservedQtyError) Text("Reserved quantity cannot be greater than total quantity.", color = MaterialTheme.colorScheme.error) }
                    )
                    OutlinedTextField(
                        value = remarks,
                        onValueChange = { remarks = it },
                        label = { Text("Remarks") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // --- VALIDATION LOGIC ---
                            val quantLong = quantity.toLongOrNull() ?: 0L
                            val reservedLong = reservedQuantity.toLongOrNull() ?: 0L

                            isNameError = name.isBlank()
                            isQuantityError = quantity.isBlank()
                            isReservedQtyError = reservedLong > quantLong

                            if (!isNameError && !isQuantityError && !isReservedQtyError) {
                                viewModel.saveItem(
                                    itemId = itemId,
                                    name = name,
                                    quantity = quantLong,
                                    expiryDate = expiryDate,
                                    category = category.ifBlank { null },
                                    storageLocation = storageLocation.ifBlank { null },
                                    remarks = remarks.ifBlank { null },
                                    contactMethod = if (convertToDonation) contactMethod.ifBlank { null } else null,
                                    pickupLocation = if (convertToDonation) pickupLocation.ifBlank { null } else null,
                                    convertToDonation = convertToDonation,
                                    reservedQuantity = reservedLong
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text("Save Item")
                    }
                }
            }
        }
    }

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.parseDateStringToMillis(expiryDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        expiryDate = datePickerState.selectedDateMillis?.let {
                            viewModel.formatMillisToDateString(it)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}