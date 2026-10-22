package com.example.autoregistroapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autoregistroapp.ui.screens.HomeScreen
import com.example.autoregistroapp.ui.screens.RegisterScreen
import com.example.autoregistroapp.ui.screens.SettingsScreen
import com.example.autoregistroapp.viewmodel.RegistroViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Register : Screen("register?registroId={registroId}") {
        fun crear(): String = "register"
        fun editar(id: String): String = "register?registroId=$id"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(viewModel: RegistroViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.crear()) },
                onNavigateToEdit = { id -> navController.navigate(Screen.Register.editar(id)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(
            route = Screen.Register.route,
            arguments = listOf(
                navArgument("registroId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            RegisterScreen(
                viewModel = viewModel,
                registroId = backStackEntry.arguments?.getString("registroId"),
                onRegistroGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}