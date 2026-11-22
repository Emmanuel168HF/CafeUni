package com.cafeuni.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cafeuni.app.data.repository.UsuarioRepository
import com.cafeuni.app.screens.DashboardScreen
import com.cafeuni.app.screens.LoginScreen
import com.cafeuni.app.screens.RegisterScreen
import com.cafeuni.app.session.SessionManager

/**
 * Rutas de navegación de la aplicación Café Uni.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
}

/**
 * Grafo de navegación entre las pantallas de la aplicación.
 * Evalúa la sesión activa mediante `SessionManager` para definir la pantalla inicial.
 */
@Composable
fun AppNavigation(
    usuarioRepository: UsuarioRepository,
    sessionManager: SessionManager,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedInState by sessionManager.isLoggedInFlow.collectAsStateWithLifecycle(initialValue = null)

    val startRoute = when (isLoggedInState) {
        true -> AppRoutes.DASHBOARD
        false -> AppRoutes.LOGIN
        null -> null
    }

    if (startRoute == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        // 1. Pantalla de Inicio de Sesión
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                usuarioRepository = usuarioRepository,
                sessionManager = sessionManager,
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.REGISTER)
                },
                onNavigateToDashboard = {
                    navController.navigate(AppRoutes.DASHBOARD) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 2. Pantalla de Registro
        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                usuarioRepository = usuarioRepository,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate(AppRoutes.DASHBOARD) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. Pantalla de Dashboard
        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
                sessionManager = sessionManager,
                onLogout = {
                    // Cierra sesión y restablece la navegación a Inicio de Sesión limpiando la pila
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
