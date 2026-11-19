package com.cafeuni.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cafeuni.app.navigation.AppNavigation
import com.cafeuni.app.screens.DashboardScreen
import com.cafeuni.app.screens.LoginScreen
import com.cafeuni.app.screens.RegisterScreen
import com.cafeuni.app.ui.theme.CafeUniTheme
import dagger.hilt.android.AndroidEntryPoint // NUEVO

/**
 * Actividad Principal de Café Uni.
 *
 * Carga el tema existente (`CafeUniTheme`) y la navegación del prototipo (`AppNavigation`).
 */
@AndroidEntryPoint // NUEVO
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Aplicación del tema existente y sistema de navegación de 3 pantallas
            CafeUniTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showBackground = true, name = "Login - Modo Claro")
@Composable
fun LoginScreenPreview() {
    CafeUniTheme(darkTheme = false) {
        LoginScreen(
            onNavigateToRegister = {},
            onNavigateToDashboard = {}
        )
    }
}

@Preview(showBackground = true, name = "Registro - Modo Claro")
@Composable
fun RegisterScreenPreview() {
    CafeUniTheme(darkTheme = false) {
        RegisterScreen(
            onNavigateToLogin = {},
            onNavigateToDashboard = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Modo Claro")
@Composable
fun DashboardScreenPreview() {
    CafeUniTheme(darkTheme = false) {
        DashboardScreen(
            onLogout = {}
        )
    }
}
