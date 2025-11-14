package com.example.zerowaste.ui.registration

import android.widget.Toast
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

@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var householdSize by remember { mutableStateOf("") }

    var isUsernameError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val registrationResult by viewModel.registrationResult.observeAsState()
    LaunchedEffect(registrationResult) {
        registrationResult?.let { result ->
            result.onSuccess {
                Toast.makeText(context, "Registration Successful! Please log in.", Toast.LENGTH_LONG).show()
                onRegistrationSuccess()
            }
            result.onFailure { error ->
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }
            viewModel.clearRegistrationResult()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create an Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Username, Email, and Password fields remain the same...
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                if (isUsernameError) isUsernameError = false
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = isUsernameError,
            supportingText = { if (isUsernameError) Text("Username cannot be empty", color = MaterialTheme.colorScheme.error) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (isEmailError) isEmailError = false
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = isEmailError,
            supportingText = { if (isEmailError) Text("Email cannot be empty", color = MaterialTheme.colorScheme.error) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (isPasswordError) isPasswordError = false
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = isPasswordError,
            supportingText = { if (isPasswordError) Text("Password cannot be empty", color = MaterialTheme.colorScheme.error) }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- THIS TEXT FIELD IS NOW FIXED ---
        OutlinedTextField(
            value = householdSize,
            onValueChange = { newValue ->
                // This check ensures only strings containing digits (or an empty string) are accepted
                if (newValue.all { it.isDigit() }) {
                    householdSize = newValue
                }
            },
            label = { Text("Household Size (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                isUsernameError = username.isBlank()
                isEmailError = email.isBlank()
                isPasswordError = password.isBlank()

                if (!isUsernameError && !isEmailError && !isPasswordError) {
                    val request = RegistrationRequest(
                        username = username,
                        password = password,
                        email = email,
                        householdSize = householdSize.toLongOrNull(),
                        twoFactorAuthEnabled = false,
                        status = "ACTIVE"
                    )
                    viewModel.register(request)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        ClickableText(
            text = AnnotatedString("Already have an account? Login"),
            onClick = { onNavigateToLogin() },
            style = TextStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)
        )
    }
}

