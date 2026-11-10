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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.ui.screens.ConsentScreen
import com.example.autoregistroapp.ui.screens.HomeScreen
import com.example.autoregistroapp.ui.screens.LoginScreen
import com.example.autoregistroapp.ui.screens.MisDatosScreen
import com.example.autoregistroapp.ui.screens.ProfileScreen
import com.example.autoregistroapp.ui.screens.RegisterScreen
import com.example.autoregistroapp.ui.screens.RegistroHoyScreen
import com.example.autoregistroapp.ui.screens.SettingsScreen
import com.example.autoregistroapp.viewmodel.RegistroViewModel
import com.example.autoregistroapp.viewmodel.SesionViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Consent : Screen("consent")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object MisDatos : Screen("misdatos")
    object RegistroHoy : Screen("hoy")
    object Modulo : Screen("modulo/{ambito}?fecha={fecha}") {
        fun ir(ambito: Ambito, fecha: String? = null): String =
            "modulo/${ambito.name}" + (fecha?.let { "?fecha=$it" } ?: "")
    }
}

@Composable
fun AppNavigation(
    viewModel: RegistroViewModel,
    sesionViewModel: SesionViewModel
) {
    val navController = rememberNavController()
    val sesion by sesionViewModel.sesion.collectAsStateWithLifecycle()
    val entrada by navController.currentBackStackEntryAsState()

    // Guardias: sin sesión -> Login; sin consentimiento vigente -> Consentimiento
    // (cubre también el cierre automático por inactividad)
    LaunchedEffect(sesion, entrada) {
        val s = sesion ?: return@LaunchedEffect
        val ruta = entrada?.destination?.route ?: return@LaunchedEffect
        when {
            !s.activa && ruta != Screen.Login.route ->
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            s.activa && !s.consentimientoVigente &&
                    ruta != Screen.Consent.route && ruta != Screen.Login.route ->
                navController.navigate(Screen.Consent.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
        }
    }

    // Se decide una sola vez dónde empezar
    var inicio by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(sesion) {
        val s = sesion
        if (inicio == null && s != null) {
            inicio = when {
                !s.activa -> Screen.Login.route
                !s.consentimientoVigente -> Screen.Consent.route
                else -> Screen.Home.route
            }
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
        composable(Screen.Consent.route) {
            ConsentScreen(
                viewModel = sesionViewModel,
                onAceptado = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Consent.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onRegistroHoy = { navController.navigate(Screen.RegistroHoy.route) },
                onNavigateToModulo = { a -> navController.navigate(Screen.Modulo.ir(a)) },
                onNavigateToEdit = { a, fecha -> navController.navigate(Screen.Modulo.ir(a, fecha)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToMisDatos = { navController.navigate(Screen.MisDatos.route) }
            )
        }
        composable(Screen.RegistroHoy.route) {
            RegistroHoyScreen(
                viewModel = viewModel,
                onTerminar = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Modulo.route,
            arguments = listOf(
                navArgument("ambito") { type = NavType.StringType },
                navArgument("fecha") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val ambito = runCatching {
                Ambito.valueOf(backStackEntry.arguments?.getString("ambito") ?: "")
            }.getOrDefault(Ambito.ESTADO_ANIMO)
            RegisterScreen(
                viewModel = viewModel,
                ambito = ambito,
                fecha = backStackEntry.arguments?.getString("fecha"),
                onRegistroGuardado = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.MisDatos.route) {
            MisDatosScreen(
                sesionViewModel = sesionViewModel,
                registroViewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = sesionViewModel,
                onBack = { navController.popBackStack() },
                onCerrarSesion = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}