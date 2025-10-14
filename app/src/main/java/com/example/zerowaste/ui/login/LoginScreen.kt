package com.example.zerowaste.ui.login


import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
// --- THIS COMPOSABLE HAS BEEN REDESIGNED ---
@Composable
fun CredentialsScreen(
    onLoginClicked: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isUsernameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.Recycling,
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Welcome to ZeroWaste",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Log in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        // --- Form Section ---
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                if (isUsernameError) isUsernameError = false
            },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "Username Icon") },
            modifier = Modifier.fillMaxWidth(),
            isError = isUsernameError,
            supportingText = {
                if (isUsernameError) Text("Username cannot be empty", color = MaterialTheme.colorScheme.error)
            },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (isPasswordError) isPasswordError = false
            },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Password Icon") },
            modifier = Modifier.fillMaxWidth(),
            isError = isPasswordError,
            supportingText = {
                if (isPasswordError) Text("Password cannot be empty", color = MaterialTheme.colorScheme.error)
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )
        ClickableText(
            text = AnnotatedString("Forgot Password?"),
            onClick = { onNavigateToForgotPassword() },
            style = TextStyle(color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                isUsernameError = username.isBlank()
                isPasswordError = password.isBlank()
                if (!isUsernameError && !isPasswordError) {
                    onLoginClicked(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Login", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.weight(1f))

        // --- Footer Section ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Don't have an account?")
            Spacer(modifier = Modifier.width(4.dp))
            ClickableText(
                text = AnnotatedString("Register"),
                onClick = { onNavigateToRegister() },
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )
        }
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
