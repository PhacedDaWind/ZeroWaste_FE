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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
// ... other necessary imports
import androidx.navigation.NavController
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.login.LoginUiState

// This is the main entry point for the entire login flow
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val finalLoginResult by viewModel.finalLoginResult.observeAsState()
    LaunchedEffect(finalLoginResult) {
        finalLoginResult?.let { result ->
            result.onSuccess { token ->
                Toast.makeText(context, "Login Complete! Welcome.", Toast.LENGTH_LONG).show()
                // TODO: Navigate to the main app screen (e.g., "inventory")
                // navController.navigate("inventory") { popUpTo("login") { inclusive = true } }
            }
            result.onFailure { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    when (uiState) {
        LoginUiState.EnteringCredentials -> CredentialsScreen(
            onLoginClicked = { username, password -> viewModel.login(username, password) },
            onNavigateToRegister = { navController.navigate("register") } // Use NavController
        )
        LoginUiState.Entering2faCode -> TwoFactorAuthScreen(
            onVerifyClicked = { code -> viewModel.verify2faCode(code) }
        )
    }
}

// --- CREDENTIALS SCREEN COMPOSABLE ---
@Composable
fun CredentialsScreen(
    onLoginClicked: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onLoginClicked(username, password) }, modifier = Modifier.fillMaxWidth()) {
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