package com.example.zerowaste.ui.passwordreset

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PasswordResetFlow(
    viewModel: PasswordResetViewModel,
    onNavigateBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        if (uiState is PasswordResetState.Error) {
            Toast.makeText(context, (uiState as PasswordResetState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetFlow() // Reset to Idle after showing error
        } else if (uiState is PasswordResetState.ResetSuccess) {
            Toast.makeText(context, "Password reset successfully! Please log in.", Toast.LENGTH_LONG).show()
            onNavigateBackToLogin()
        }
    }

    // This 'when' block now handles all the new states
    when (uiState) {
        is PasswordResetState.Idle -> {
            PasswordResetRequestScreen(
                onRequestReset = { email -> viewModel.requestPasswordReset(email) },
                onNavigateBack = onNavigateBackToLogin
            )
        }
        // --- NEW: Show a spinner while the API call is in progress ---
        is PasswordResetState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        // --- NEW: Show the intermediate "Code Sent" screen ---
        is PasswordResetState.CodeSent -> {
            PasswordResetCodeSentScreen(
                onProceed = { viewModel.proceedToEnterCode() },
                onNavigateBack = onNavigateBackToLogin
            )
        }
        is PasswordResetState.AwaitingCode -> {
            PasswordResetExecuteScreen(
                onExecuteReset = { code, newPassword -> viewModel.executePasswordReset(code, newPassword) },
                onNavigateBack = onNavigateBackToLogin
            )
        }
        else -> { // Handles Success and Error by showing a loading spinner while state transitions
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// --- NEW COMPOSABLE for the "Code Sent" screen ---
@Composable
fun PasswordResetCodeSentScreen(
    onProceed: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Check Your Email", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "If an account with that email exists, a password reset code has been sent. Please check your inbox and spam folder.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onProceed, modifier = Modifier.fillMaxWidth()) {
            Text("I Have the Code")
        }
        TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Login")
        }
    }
}


@Composable
fun PasswordResetRequestScreen(
    onRequestReset: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    // 1. NEW state to track if the email field has an error
    var isError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Reset Password", style = MaterialTheme.typography.headlineMedium)
        Text("Enter your email address to receive a password reset code.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // 2. UPDATED OutlinedTextField to show error state
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // Clear the error as the user types
                if (isError) isError = false
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = isError, // This will turn the field red if there's an error
            supportingText = {
                if (isError) {
                    Text("Email cannot be empty", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 3. UPDATED Button with validation logic
        Button(
            onClick = {
                if (email.isBlank()) {
                    // If email is empty, set the error state and do not call the API
                    isError = true
                } else {
                    // If email is valid, proceed with the API call
                    onRequestReset(email)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Code")
        }
        TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Login")
        }
    }
}

@Composable
fun PasswordResetExecuteScreen(
    onExecuteReset: (String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Enter Code", style = MaterialTheme.typography.headlineMedium)
        Text("Check your email for the code and enter it below along with your new password.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Reset Code") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onExecuteReset(code, newPassword) }, modifier = Modifier.fillMaxWidth()) {
            Text("Reset Password")
        }
        TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back to Login")
        }
    }
}
