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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.zerowaste.data.local.SessionManager
import com.example.zerowaste.ui.browse.BrowseFoodItemScreen
import com.example.zerowaste.ui.browse.BrowseFoodItemViewModel
import com.example.zerowaste.ui.fooddetail.FoodItemDetailScreen
import com.example.zerowaste.ui.fooddetail.FoodItemDetailViewModel
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
import com.example.zerowaste.ui.screens.main.SettingsViewModel
import com.example.zerowaste.ui.setting.SettingsScreen
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
                        loginViewModel,
                        registrationViewModel,
                        homeViewModel,
                        settingsViewModel,
                        notificationViewModel,
                        browseFoodItemViewModel,
                        passwordResetViewModel
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
    passwordResetViewModel: PasswordResetViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginFlow(
                viewModel = loginViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("password_reset") },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
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

        navigation(startDestination = "main_screens", route = "main") {
            composable("main_screens") {
                // The onLogout logic is now defined here
                MainAppScreen(
                    homeViewModel = homeViewModel,
                    settingsViewModel = settingsViewModel,
                    loginViewModel = loginViewModel,
                    notificationViewModel = notificationViewModel,
                    browseFoodItemViewModel = browseFoodItemViewModel,
                    onLogout = {
                        loginViewModel.clearLoginResult()
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    // We also pass the main NavController for detail navigation
                    appNavController = navController
                )
            }
            composable(
                route = "food_detail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: FoodItemDetailViewModel = viewModel()
                val itemId = backStackEntry.arguments?.getLong("itemId")
                if (itemId != null) {
                    FoodItemDetailScreen(
                        itemId = itemId,
                        viewModel = viewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(
    appNavController: NavController,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    loginViewModel: LoginViewModel,
    notificationViewModel: NotificationViewModel,
    browseFoodItemViewModel: BrowseFoodItemViewModel,
    onLogout: () -> Unit // It now accepts both NavController and onLogout
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController = bottomNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            BottomNavGraph(
                // --- THIS IS THE FIX ---
                // We pass the CORRECT appNavController down from the parameters
                appNavController = appNavController,
                bottomNavController = bottomNavController,
                homeViewModel = homeViewModel,
                settingsViewModel = settingsViewModel,
                loginViewModel = loginViewModel,
                notificationViewModel = notificationViewModel,
                browseFoodItemViewModel = browseFoodItemViewModel,
                onLogout = onLogout // And we also pass the onLogout lambda
            )
        }
    }
}
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
    loginViewModel: LoginViewModel,
    notificationViewModel: NotificationViewModel,
    browseFoodItemViewModel: BrowseFoodItemViewModel,
    onLogout: () -> Unit // It correctly accepts the onLogout lambda
) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(viewModel = homeViewModel)
        }
        composable(route = BottomBarScreen.Browse.route) {
            BrowseFoodItemScreen(
                viewModel = browseFoodItemViewModel,
                appNavController = appNavController
            )
        }
        composable(route = BottomBarScreen.Notifications.route) {
            NotificationScreen(viewModel = notificationViewModel)
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onLogout = onLogout // It correctly passes the lambda to the SettingsScreen
            )
        }
    }
}