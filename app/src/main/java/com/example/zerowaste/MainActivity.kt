package com.example.zerowaste

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import com.example.zerowaste.data.remote.RegistrationRequest
import com.example.zerowaste.ui.login.CredentialsScreen
import com.example.zerowaste.ui.login.LoginUiState
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.login.TwoFactorAuthScreen
import com.example.zerowaste.ui.registration.RegistrationScreen
import com.example.zerowaste.ui.registration.RegistrationViewModel
import com.example.zerowaste.ui.theme.ZeroWasteTheme

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroWasteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // This state controls which main screen is shown (Login or Register)
                    var showLoginScreen by remember { mutableStateOf(true) }

                    if (showLoginScreen) {
                        LoginFlow(
                            viewModel = loginViewModel,
                            onNavigateToRegister = { showLoginScreen = false } // Lambda to switch to register
                        )
                    } else {
                        RegistrationScreen(
                            viewModel = registrationViewModel,
                            onNavigateToLogin = { showLoginScreen = true }, // Lambda to switch back to login
                            onRegistrationSuccess = {
                                // After successful registration, automatically switch to the login screen
                                showLoginScreen = true
                            }
                        )
                    }
                }
            }
        }
    }
}


// --- LOGIN FLOW COMPOSABLE (Navigator for Login/2FA) ---
@Composable
fun LoginFlow(viewModel: LoginViewModel, onNavigateToRegister: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val finalLoginResult by viewModel.finalLoginResult.observeAsState()
    LaunchedEffect(finalLoginResult) {
        finalLoginResult?.let { result ->
            result.onSuccess { token ->
                Toast.makeText(context, "Login Complete! Welcome.", Toast.LENGTH_LONG).show()
                // TODO: Save the token and navigate to the main part of the app
            }
            result.onFailure { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // This 'when' block automatically shows the correct screen based on ViewModel state
    when (uiState) {
        LoginUiState.EnteringCredentials -> CredentialsScreen(
            onLoginClicked = { username, password -> viewModel.login(username, password) },
            onNavigateToRegister = onNavigateToRegister
        )
        LoginUiState.Entering2faCode -> TwoFactorAuthScreen(
            onVerifyClicked = { code -> viewModel.verify2faCode(code) }
        )
    }
}



