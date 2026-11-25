package com.cafeuni.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cafeuni.app.data.dao.PedidoDao
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
    pedidoDao: PedidoDao,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedInState by sessionManager.isLoggedInFlow.collectAsStateWithLifecycle(initialValue = false)

    val startRoute = if (isLoggedInState) AppRoutes.DASHBOARD else AppRoutes.LOGIN

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
                }
            )
        }

        // 3. Pantalla de Dashboard
        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
                sessionManager = sessionManager,
                usuarioRepository = usuarioRepository,
                pedidoDao = pedidoDao,
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
