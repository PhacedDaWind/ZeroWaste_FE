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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.theme.ZeroWasteTheme

class MainActivity : ComponentActivity() {
    // Use the by viewModels() delegate to get a reference to the ViewModel
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroWasteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Pass the ViewModel to our new LoginScreen
                    LoginScreen(viewModel = loginViewModel)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    // 1. State for the text fields. 'remember' makes Compose keep track of the values.
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Observe the login result from the ViewModel
    val loginResult by viewModel.loginResult.observeAsState()

    // Handle the result to show a Toast message
    loginResult?.let { result ->
        result.onSuccess { token ->
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_LONG).show()
            // You can now save the token and navigate to the next screen
        }
        result.onFailure { error ->
            Toast.makeText(context, "Login Failed: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 2. The UI layout for the login screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            visualTransformation = PasswordVisualTransformation() // Hides the password text
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // 3. When the button is clicked, call the ViewModel's login function
                viewModel.login(username, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}
