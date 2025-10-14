package com.example.zerowaste

// Import the new ViewModels
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.ui.home.HomeScreen
import com.example.zerowaste.ui.home.HomeViewModel
import com.example.zerowaste.ui.login.LoginFlow
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.notification.NotificationScreen
import com.example.zerowaste.ui.notification.NotificationViewModel
import com.example.zerowaste.ui.passwordreset.PasswordResetFlow
import com.example.zerowaste.ui.passwordreset.PasswordResetViewModel
import com.example.zerowaste.ui.registration.RegistrationScreen
import com.example.zerowaste.ui.registration.RegistrationViewModel
import com.example.zerowaste.ui.screens.BrowseFoodItemScreen
import com.example.zerowaste.ui.screens.main.SettingsViewModel
import com.example.zerowaste.ui.setting.SettingsScreen
import com.example.zerowaste.ui.theme.ZeroWasteTheme
import com.example.zerowaste.viewmodel.BrowseFoodItemViewModel

// Sealed class for Bottom Bar routes remains the same
sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home", "Home", Icons.Default.Home)
    object Browse : BottomBarScreen("browse", "Browse", Icons.Default.Search)
    object Notifications : BottomBarScreen("notifications", "Notifications", Icons.Default.Notifications)
    object Settings : BottomBarScreen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val notificationViewModel: NotificationViewModel by viewModels()
    private val browseFoodItemViewModel: BrowseFoodItemViewModel by viewModels()
    private val passwordResetViewModel: PasswordResetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZeroWasteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        loginViewModel = loginViewModel,
                        registrationViewModel = registrationViewModel,
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel,
                        notificationViewModel = notificationViewModel,
                        browseFoodItemViewModel = browseFoodItemViewModel,
                        passwordResetViewModel = passwordResetViewModel // Pass it down
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    registrationViewModel: RegistrationViewModel,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    notificationViewModel: NotificationViewModel,
    browseFoodItemViewModel: BrowseFoodItemViewModel,
    passwordResetViewModel: PasswordResetViewModel // Receive
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        navigation(startDestination = "login", route = "auth") {
            composable("login") {
                LoginFlow(
                    viewModel = loginViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    // --- NEW: Add navigation to the password reset flow ---
                    onNavigateToForgotPassword = { navController.navigate("password_reset") },
                    onLoginSuccess = {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") {
                RegistrationScreen(
                    viewModel = registrationViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegistrationSuccess = { navController.popBackStack() }
                )
            }
            composable("password_reset") {
                PasswordResetFlow(
                    viewModel = passwordResetViewModel,
                    onNavigateBackToLogin = {
                        passwordResetViewModel.resetFlow()
                        navController.popBackStack()
                    }
                )
            }
        }
        composable("main") {
            MainAppScreen(
                appNavController = navController,
                homeViewModel = homeViewModel,
                settingsViewModel = settingsViewModel,
                loginViewModel = loginViewModel,
                notificationViewModel = notificationViewModel,
                browseFoodItemViewModel = browseFoodItemViewModel
            )
        }
    }
}

@Composable
fun MainAppScreen(
    appNavController: NavController,
    homeViewModel: HomeViewModel, // Pass down
    settingsViewModel: SettingsViewModel, // Pass down
    loginViewModel: LoginViewModel,
    notificationViewModel: NotificationViewModel,
    browseFoodItemViewModel: BrowseFoodItemViewModel
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomNavGraph(
                appNavController = appNavController,
                bottomNavController = bottomNavController,
                homeViewModel = homeViewModel,
                settingsViewModel = settingsViewModel,
                loginViewModel = loginViewModel, // <-- Pass LoginViewModel to the bottom graph
                notificationViewModel=notificationViewModel,
                browseFoodItemViewModel = browseFoodItemViewModel// Pass down
            )
        }
    }
}
// BottomBar composable remains the same...
@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Browse,
        BottomBarScreen.Notifications,
        BottomBarScreen.Settings,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}



@Composable
fun BottomNavGraph(
    appNavController: NavController,
    bottomNavController: NavHostController,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel, // <-- Receive LoginViewModel
    notificationViewModel: NotificationViewModel,
    browseFoodItemViewModel: BrowseFoodItemViewModel// Receive
) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(viewModel = homeViewModel)
        }
        composable(route = BottomBarScreen.Browse.route) {
            val context = LocalContext.current
            val sessionManager = SessionManager(context)
            val userId = sessionManager.getUserId() ?: 0L

            BrowseFoodItemScreen(
                userId = userId,
                viewModel = browseFoodItemViewModel
            )
        }
        composable(route = BottomBarScreen.Notifications.route) {
            val context = LocalContext.current
            val sessionManager = SessionManager(context)
            val userId = sessionManager.getUserId() ?: 0L
            NotificationScreen(
                userId = userId,
                viewModel = notificationViewModel
            )
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onLogout = {
                    // --- THIS IS THE KEY FIX ---
                    // 1. Clear the old login state
                    loginViewModel.clearLoginResult()
                    // 2. Navigate back to the auth flow
                    appNavController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

