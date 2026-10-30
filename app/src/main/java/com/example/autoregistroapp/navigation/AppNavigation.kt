package com.example.autoregistroapp.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autoregistroapp.ui.screens.HomeScreen
import com.example.autoregistroapp.ui.screens.LoginScreen
import com.example.autoregistroapp.ui.screens.ProfileScreen
import com.example.autoregistroapp.ui.screens.RegisterScreen
import com.example.autoregistroapp.ui.screens.SettingsScreen
import com.example.autoregistroapp.viewmodel.RegistroViewModel
import com.example.autoregistroapp.viewmodel.SesionViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Register : Screen("register?registroId={registroId}") {
        fun crear(): String = "register"
        fun editar(id: String): String = "register?registroId=$id"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    viewModel: RegistroViewModel,
    sesionViewModel: SesionViewModel
) {
    val navController = rememberNavController()
    val sesion by sesionViewModel.sesion.collectAsStateWithLifecycle()

    // Se decide una sola vez dónde empezar: Home si ya hay sesión, Login si no
    var inicio by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(sesion) {
        val s = sesion
        if (inicio == null && s != null) {
            inicio = if (s.activa) Screen.Home.route else Screen.Login.route
        }
    }

    val destinoInicial = inicio
    if (destinoInicial == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
        },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = sesionViewModel,
                onLoginExitoso = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.crear()) },
                onNavigateToEdit = { id -> navController.navigate(Screen.Register.editar(id)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
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
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = sesionViewModel,
                onBack = { navController.popBackStack() },
                onCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
