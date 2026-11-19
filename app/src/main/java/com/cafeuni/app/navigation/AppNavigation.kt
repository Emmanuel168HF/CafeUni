package com.cafeuni.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cafeuni.app.screens.DashboardScreen
import com.cafeuni.app.screens.LoginScreen
import com.cafeuni.app.screens.RegisterScreen

/**
 * Ritas de navegación de la aplicación Café Uni.
 */
object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
}

/**
 * Grafo de navegación principal entre las tres pantallas del prototipo:
 * 1. LoginScreen (Inicio de sesión)
 * 2. RegisterScreen (Registro)
 * 3. DashboardScreen (Panel administrativo visual)
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LOGIN
    ) {
        // 1. Pantalla de Inicio de Sesión
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.REGISTER)
                },
                onNavigateToDashboard = {
                    navController.navigate(AppRoutes.DASHBOARD)
                }
            )
        }

        // 2. Pantalla de Registro
        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToDashboard = {
                    navController.navigate(AppRoutes.DASHBOARD)
                }
            )
        }

        // 3. Pantalla de Dashboard
        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
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
