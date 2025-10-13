package com.example.zerowaste

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.zerowaste.ui.login.LoginUiState
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.theme.ZeroWasteTheme

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroWasteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginFlow(viewModel = loginViewModel)
                }
            }
        }
    }
}

@Composable
fun LoginFlow(viewModel: LoginViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Observe the final login result for showing Toasts.
    val finalLoginResult by viewModel.finalLoginResult.observeAsState()

    // Use LaunchedEffect to show the toast only once per result change
    LaunchedEffect(finalLoginResult) {
        finalLoginResult?.let { result ->
            result.onSuccess {
                // This is a good place to save the token and navigate to the main app screen.
                Toast.makeText(context, "Login Complete! Welcome.", Toast.LENGTH_LONG).show()
            }
            result.onFailure { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Show the correct UI based on the state from the ViewModel.
    when (uiState) {
        LoginUiState.EnteringCredentials -> CredentialsScreen(
            onLoginClicked = { username, password ->
                viewModel.login(username, password)
            }
        )
        LoginUiState.Entering2faCode -> TwoFactorAuthScreen(
            onVerifyClicked = { code ->
                viewModel.verify2faCode(code)
            }
        )
    }
}

@Composable
fun CredentialsScreen(onLoginClicked: (String, String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Your Credentials")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onLoginClicked(username, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

@Composable
fun TwoFactorAuthScreen(onVerifyClicked: (String) -> Unit) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter the 2FA code sent to your device")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("6-Digit Code") },
            modifier = Modifier.fillMaxWidth()
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

