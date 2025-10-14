package com.example.zerowaste.ui.setting

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.zerowaste.ui.screens.main.SettingsViewModel
import com.example.zerowaste.ui.screens.main.TwoFactorSetupState
import com.example.zerowaste.ui.theme.ZeroWasteTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserSettings()
    }
    // This effect handles the one-time logout navigation
    LaunchedEffect(uiState.logoutCompleted) {
        if (uiState.logoutCompleted) {
            onLogout()
            viewModel.resetLogoutState()
        }
    }

    // --- THIS IS THE KEY FIX ---
    // This effect now shows the error message and then immediately resets the state.
    LaunchedEffect(uiState.twoFactorSetupState) {
        if (uiState.twoFactorSetupState is TwoFactorSetupState.Error) {
            // 1. Show the error message to the user
            Toast.makeText(context, (uiState.twoFactorSetupState as TwoFactorSetupState.Error).message, Toast.LENGTH_LONG).show()
            // 2. Reset the state so the message doesn't show again
            viewModel.clearTwoFactorState()
        }
    }

    // Main UI content
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
                onCheckedChange = { isEnabled ->
                    if (isEnabled) {
                        viewModel.onEnable2faClicked()
                    } else {
                        viewModel.onDisable2faClicked()
                    }
                }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))


            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { viewModel.onLogoutClicked() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        }

        // --- Loading Indicator and Dialogs ---
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Show the 2FA verification dialog when the state requires it
        if (uiState.twoFactorSetupState is TwoFactorSetupState.AwaitingVerification) {
            TwoFactorVerificationDialog(
                onVerify = { code -> viewModel.onVerify2faSetup(code) },
                onDismiss = { viewModel.cancel2faSetup() }
            )
        }
    }
}


// --- NEW COMPOSABLE for the 2FA Verification Dialog ---
@Composable
fun TwoFactorVerificationDialog(
    onVerify: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Enable 2FA",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("A verification code has been sent to your email. Please enter it below to complete the setup.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Verification Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onVerify(code) }) {
                        Text("Verify")
                    }
                }
            }
        }
    }
}


// --- Reusable Composables (No Changes) ---
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
        Text("Settings Screen Preview (Requires ViewModel)")
    }
}

