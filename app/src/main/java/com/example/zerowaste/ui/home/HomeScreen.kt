package com.example.zerowaste.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.zerowaste.R
import com.example.zerowaste.data.remote.ExpiringItemResponse
import com.example.zerowaste.ui.theme.ZeroWasteTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToInventory: () -> Unit // <-- 1. NEW PARAMETER
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHomeScreenData()
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { HeaderSection(username = uiState.username) }
            item { ActivitySummarySection(totalItems = uiState.totalItems, donationsMade = uiState.donationsMade) }
            // 2. Pass the callback to the actions section
            item { MainActionsSection(onNavigateToInventory = onNavigateToInventory) }
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
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
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
fun MainActionsSection(onNavigateToInventory: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 3. Trigger the callback when clicked
        ActionButton(text = "Food Inventory", onClick = onNavigateToInventory)
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
        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 16.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ExpiryListSection(items: List<ExpiringItemResponse>) {
    Column {
        Text("Food expiry soon listing:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("Quantity", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                    Text("Expiry date", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                }
                Divider()
                if (items.isEmpty()) {
                    Text(
                        "No items expiring soon.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    items.forEach { item ->
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(item.foodName, modifier = Modifier.weight(2f))
                            Text(item.quantity.toString(), modifier = Modifier.weight(1.5f))
                            val color = if (item.daysUntilExpiry.contains("Today") || item.daysUntilExpiry.contains("Expired"))
                                Color.Red else Color.Unspecified
                            Text(item.daysUntilExpiry, modifier = Modifier.weight(1.5f), color = color)
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ZeroWasteTheme {
        Text("Home Screen Preview")
    }
}