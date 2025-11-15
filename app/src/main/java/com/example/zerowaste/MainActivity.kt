package com.example.zerowaste

// Import the new ViewModels
import AddEditFoodItemScreen
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.zerowaste.ui.browse.BrowseFoodItemScreen
import com.example.zerowaste.ui.browse.BrowseFoodItemViewModel
import com.example.zerowaste.ui.fooddetail.FoodItemDetailScreen
import com.example.zerowaste.ui.fooddetail.FoodItemDetailViewModel
import com.example.zerowaste.ui.home.HomeScreen
import com.example.zerowaste.ui.home.HomeViewModel
import com.example.zerowaste.ui.inventory.AddEditFoodItemViewModel
import com.example.zerowaste.ui.inventory.FoodInventoryScreen
import com.example.zerowaste.ui.inventory.FoodInventoryViewModel
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


// Sealed class for Bottom Bar routes
sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home", "Home", Icons.Default.Home)
    object Browse : BottomBarScreen("browse", "Browse", Icons.Default.Search)
    object Inventory : BottomBarScreen("inventory", "My Items", Icons.Default.Inventory)
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
    private val foodInventoryViewModel: FoodInventoryViewModel by viewModels()
    // AddEdit and Detail ViewModels are scoped to their routes, not created here.

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
                        passwordResetViewModel,
                        foodInventoryViewModel
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
    passwordResetViewModel: PasswordResetViewModel,
    foodInventoryViewModel: FoodInventoryViewModel
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

        // --- MAIN APPLICATION GRAPH ---
        navigation(startDestination = "main_screens", route = "main") {
            composable("main_screens") {
                MainAppScreen(
                    appNavController = navController,
                    homeViewModel = homeViewModel,
                    settingsViewModel = settingsViewModel,
                    loginViewModel = loginViewModel,
                    notificationViewModel = notificationViewModel,
                    browseFoodItemViewModel = browseFoodItemViewModel,
                    foodInventoryViewModel = foodInventoryViewModel,
                    onLogout = {
                        loginViewModel.clearLoginResult()
                        navController.navigate("login") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            // Route for PUBLIC item detail (from Browse screen)
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
            // Route for ADDING a new item (from Inventory screen)
            composable(route = "add_food_item") {
                val viewModel: AddEditFoodItemViewModel = viewModel()
                AddEditFoodItemScreen(
                    itemId = null, // null signifies "Create" mode
                    viewModel = viewModel,
                    navController = navController
                )
            }
            // Route for EDITING an item (from Inventory screen)
            composable(
                route = "edit_food_item/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStackEntry ->
                val viewModel: AddEditFoodItemViewModel = viewModel()
                val itemId = backStackEntry.arguments?.getLong("itemId")
                if (itemId != null) {
                    AddEditFoodItemScreen(
                        itemId = itemId, // Pass the ID for "Edit" mode
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
    foodInventoryViewModel: FoodInventoryViewModel,
    onLogout: () -> Unit
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
                loginViewModel = loginViewModel,
                notificationViewModel = notificationViewModel,
                browseFoodItemViewModel = browseFoodItemViewModel,
                foodInventoryViewModel = foodInventoryViewModel,
                onLogout = onLogout
            )
        }
    }
}
@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Browse,
        BottomBarScreen.Inventory,
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
    foodInventoryViewModel: FoodInventoryViewModel,
    onLogout: () -> Unit
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
        composable(route = BottomBarScreen.Inventory.route) {
            FoodInventoryScreen(
                viewModel = foodInventoryViewModel,
                navController = bottomNavController,
                    onNavigateToAddItem = {
                appNavController.navigate("add_food_item")
            },
            onNavigateToEditItem = { itemId ->
                // Navigates to the correct "edit" route
                appNavController.navigate("edit_food_item/$itemId")
            }
            )
        }
        composable(route = BottomBarScreen.Notifications.route) {
            NotificationScreen(viewModel = notificationViewModel)
        }
        composable(route = BottomBarScreen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onLogout = onLogout
            )
        }
    }
}