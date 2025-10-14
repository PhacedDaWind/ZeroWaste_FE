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
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.zerowaste.ui.home.HomeScreen
import com.example.zerowaste.ui.home.HomeViewModel
import com.example.zerowaste.ui.login.LoginFlow
import com.example.zerowaste.ui.login.LoginViewModel
import com.example.zerowaste.ui.notification.NotificationScreen
import com.example.zerowaste.ui.notification.NotificationViewModel
import com.example.zerowaste.ui.registration.RegistrationScreen
import com.example.zerowaste.ui.registration.RegistrationViewModel
import com.example.zerowaste.ui.setting.SettingsScreen
import com.example.zerowaste.ui.setting.SettingsViewModel
import com.example.zerowaste.ui.theme.ZeroWasteTheme

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
    // --- NEW: Instantiate Home and Settings ViewModels ---
    private val homeViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    // --- 2. Create an instance of NotificationViewModel ---
    private val notificationViewModel: NotificationViewModel by viewModels()

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
                        notificationViewModel = notificationViewModel
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
    homeViewModel: HomeViewModel, // Pass down
    settingsViewModel: SettingsViewModel,
    notificationViewModel: NotificationViewModel// Pass down
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        navigation(startDestination = "login", route = "auth") {
            composable("login") {
                LoginFlow(
                    viewModel = loginViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
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
        }
        composable("main") {
            MainAppScreen(
                appNavController = navController,
                homeViewModel = homeViewModel, // Pass down
                settingsViewModel = settingsViewModel,
                notificationViewModel= notificationViewModel// Pass down
            )
        }
    }
}


@Composable
fun MainAppScreen(
    appNavController: NavController,
    homeViewModel: HomeViewModel, // Pass down
    settingsViewModel: SettingsViewModel,
    notificationViewModel: NotificationViewModel// Pass down
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomNavGraph(
                appNavController = appNavController,
                bottomNavController = bottomNavController,
                homeViewModel = homeViewModel, // Pass down
                settingsViewModel = settingsViewModel,
                notificationViewModel=notificationViewModel// Pass down
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
    homeViewModel: HomeViewModel, // Receive
    settingsViewModel: SettingsViewModel,
    notificationViewModel: NotificationViewModel// Receive
) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            // Pass the ViewModel to the screen
            HomeScreen(viewModel = homeViewModel)
        }
        composable(route = BottomBarScreen.Browse.route) { Text("Browse Screen") }
        composable(route = BottomBarScreen.Notifications.route) {
            // TODO: Replace '1L' with the actual logged-in user ID from your session state
            NotificationScreen(
                currentUserId = 1L,
                viewModel = notificationViewModel
            )
        }
        composable(route = BottomBarScreen.Settings.route) {
            // Pass the ViewModel to the screen
            SettingsScreen(
                viewModel = settingsViewModel,
                onLogout = {
                    appNavController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}

