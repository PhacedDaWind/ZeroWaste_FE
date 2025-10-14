package com.example.zerowaste.ui.login


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
// ... other necessary imports
import androidx.navigation.NavController
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.login.LoginUiState

// This is the main entry point for the entire login flow
@Composable
fun LoginFlow(
    viewModel: LoginViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit, // <-- 1. ADD THIS PARAMETER
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val finalLoginResult by viewModel.finalLoginResult.observeAsState()
    LaunchedEffect(finalLoginResult) {
        finalLoginResult?.let { result ->
            result.onSuccess { token ->
                Toast.makeText(context, "Login Complete! Welcome.", Toast.LENGTH_LONG).show()
                onLoginSuccess()
            }
            result.onFailure { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.clearLoginResult()
    }

    when (uiState) {
        LoginUiState.EnteringCredentials -> CredentialsScreen(
            onLoginClicked = { username, password -> viewModel.login(username, password) },
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToForgotPassword = onNavigateToForgotPassword // <-- 2. PASS IT DOWN
        )
        LoginUiState.Entering2faCode -> TwoFactorAuthScreen(
            onVerifyClicked = { code -> viewModel.verify2faCode(code) }
        )
    }
}

// --- THIS COMPOSABLE IS UPDATED ---
@Composable
fun CredentialsScreen(
    onLoginClicked: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // 1. NEW state variables to track errors
    var isUsernameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Zerowaste", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // 2. UPDATED Username field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                if (isUsernameError) isUsernameError = false // Clear error on type
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = isUsernameError,
            supportingText = {
                if (isUsernameError) Text("Username cannot be empty", color = MaterialTheme.colorScheme.error)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 3. UPDATED Password field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (isPasswordError) isPasswordError = false // Clear error on type
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = isPasswordError,
            supportingText = {
                if (isPasswordError) Text("Password cannot be empty", color = MaterialTheme.colorScheme.error)
            }
        )

        ClickableText(
            text = AnnotatedString("Forgot Password?"),
            onClick = { onNavigateToForgotPassword() },
            style = TextStyle(color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. UPDATED Button with validation logic
        Button(
            onClick = {
                // Reset errors before checking
                isUsernameError = username.isBlank()
                isPasswordError = password.isBlank()

                // Only call the API if both fields are valid
                if (!isUsernameError && !isPasswordError) {
                    onLoginClicked(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        ClickableText(
            text = AnnotatedString("Don't have an account? Register"),
            onClick = { onNavigateToRegister() },
            style = TextStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
        )
    }
}

// --- 2FA SCREEN COMPOSABLE ---
@Composable
fun TwoFactorAuthScreen(onVerifyClicked: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter 2FA Code", style = MaterialTheme.typography.headlineMedium)
        Text("Check your email for the verification code.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("6-Digit Code") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onVerifyClicked(code) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify")
        }
    }
}
