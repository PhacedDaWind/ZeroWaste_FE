package com.example.zerowaste.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close // CORRECTED: Changed from Cancel to Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerowaste.R // Make sure to have a placeholder image in res/drawable
import com.example.zerowaste.ui.theme.ZeroWasteTheme

// Data class for expiring items remains here for now
data class ExpiringItem(val name: String, val quantity: String, val expiryDate: String)

@Composable
fun HomeScreen(viewModel: HomeViewModel) { // <-- 1. ViewModel is now a parameter
    // 2. Collect the state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // 3. Show a loading indicator while data is being fetched
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null) {
        // Show an error message if something went wrong
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    } else {
        // 4. Once loaded, display the main content using data from the uiState
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { HeaderSection(username = uiState.username) }
            item { ActivitySummarySection(totalItems = uiState.totalItems, donationsMade = uiState.donationsMade) }
            item { MainActionsSection() }
            item { ExpiryListSection(items = uiState.expiringItems) }
        }
    }
}

@Composable
fun HeaderSection(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Replace with your actual profile picture logic
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Placeholder image
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Hello,\n$username",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp
            )
        }
        IconButton(onClick = { /* TODO: Handle info click */ }) {
            Icon(Icons.Default.Info, contentDescription = "Info")
        }
    }
}

@Composable
fun ActivitySummarySection(totalItems: Int, donationsMade: Int) {
    Column {
        Text("Activity Summary", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(title = "Total Items", value = totalItems.toString(), modifier = Modifier.weight(1f))
            SummaryCard(title = "Donations Made", value = donationsMade.toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MainActionsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionButton(text = "Food Inventory", onClick = { /* TODO */ })
        ActionButton(text = "Track and Report", onClick = { /* TODO */ })
        ActionButton(text = "Plan Weekly Meal", onClick = { /* TODO */ })
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray) // CORRECTED: Changed from Cancel to Close
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ExpiryListSection(items: List<ExpiringItem>) {
    Column {
        Text("Food expiry soon listing:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column {
                // Header Row
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("Quantity", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("Expiry date", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                }
                Divider()
                // Data Rows
                items.forEach { item ->
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(item.name, modifier = Modifier.weight(2f))
                        Text(item.quantity, modifier = Modifier.weight(1.5f))
                        Text(item.expiryDate, modifier = Modifier.weight(1.5f))
                    }
                    Divider()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ZeroWasteTheme {
        // Preview still works without a ViewModel
        // HomeScreen(viewModel = HomeViewModel()) // This would require more setup for previews
        Text("Home Screen Preview (Connect ViewModel for full preview)")
    }
}

