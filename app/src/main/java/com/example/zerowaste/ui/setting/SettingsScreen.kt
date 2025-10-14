package com.example.zerowaste.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zerowaste.ui.theme.ZeroWasteTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel, // <-- 1. ViewModel is now a parameter
    onLogout: () -> Unit // Callback function to trigger logout navigation
) {
    // 2. Collect the state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // 3. Trigger logout navigation when the ViewModel's state changes
    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            onLogout()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- Account Section ---
            Text(
                "Account",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsToggleItem(
                icon = Icons.Default.Lock,
                title = "Enable Two-Factor Authentication",
                checked = uiState.is2faEnabled,
                // 4. Call the ViewModel function on interaction
                onCheckedChange = { viewModel.on2faToggleChanged(it) }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- Notifications Section ---
            Text(
                "Notifications",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsClickableItem(
                icon = Icons.Default.Notifications,
                title = "Notification Settings",
                onClick = { /* TODO: Navigate to notification settings screen */ }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Logout Button ---
            OutlinedButton(
                // 5. Call the ViewModel function on click
                onClick = { viewModel.onLogoutClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }

        // 6. Show a loading overlay if any operation is in progress
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// A reusable composable for settings items with a Switch
@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// A reusable composable for settings items that navigate somewhere else
@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ArrowForward, contentDescription = "Go to", modifier = Modifier.size(16.dp))
    }
}



@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ZeroWasteTheme {
        // Preview works without a ViewModel, but shows the base state
        Text("Settings Screen Preview (Connect ViewModel for full preview)")
    }
}

