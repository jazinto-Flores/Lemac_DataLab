package com.example.autoregistroapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.autoregistroapp.ui.screens.HomeScreen
import com.example.autoregistroapp.ui.screens.RegisterScreen
import com.example.autoregistroapp.ui.screens.SettingsScreen
import com.example.autoregistroapp.viewmodel.RegistroViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Register : Screen("register")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(viewModel: RegistroViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}