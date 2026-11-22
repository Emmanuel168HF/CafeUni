package com.cafeuni.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.cafeuni.app.data.database.DatabaseHelper
import com.cafeuni.app.data.repository.UsuarioRepository
import com.cafeuni.app.data.security.PasswordHasher
import com.cafeuni.app.data.dao.UsuarioDao
import com.cafeuni.app.navigation.AppNavigation
import com.cafeuni.app.screens.DashboardScreen
import com.cafeuni.app.screens.LoginScreen
import com.cafeuni.app.screens.RegisterScreen
import com.cafeuni.app.session.SessionManager
import com.cafeuni.app.ui.theme.CafeUniTheme
import dagger.hilt.android.AndroidEntryPoint // NUEVO
import javax.inject.Inject // NUEVO

/**
 * Actividad Principal de Café Uni.
 *
 * Carga el tema existente (`CafeUniTheme`) y la navegación del prototipo (`AppNavigation`).
 */
@AndroidEntryPoint // NUEVO
class MainActivity : ComponentActivity() {

    @Inject lateinit var usuarioRepository: UsuarioRepository // NUEVO
    @Inject lateinit var sessionManager: SessionManager // NUEVO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Aplicación del tema existente y sistema de navegación
            CafeUniTheme {
                AppNavigation(
                    usuarioRepository = usuarioRepository,
                    sessionManager = sessionManager
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Login - Modo Claro")
@Composable
fun LoginScreenPreview() {
    val context = LocalContext.current
    val repository = UsuarioRepository(UsuarioDao(DatabaseHelper(context)), PasswordHasher())
    val session = SessionManager(context)
    CafeUniTheme(darkTheme = false) {
        LoginScreen(
            usuarioRepository = repository,
            sessionManager = session,
            onNavigateToRegister = {},
            onNavigateToDashboard = {}
        )
    }
}

@Preview(showBackground = true, name = "Registro - Modo Claro")
@Composable
fun RegisterScreenPreview() {
    val context = LocalContext.current
    val repository = UsuarioRepository(UsuarioDao(DatabaseHelper(context)), PasswordHasher())
    CafeUniTheme(darkTheme = false) {
        RegisterScreen(
            usuarioRepository = repository,
            onNavigateToLogin = {},
            onNavigateToDashboard = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Modo Claro")
@Composable
fun DashboardScreenPreview() {
    val context = LocalContext.current
    val session = SessionManager(context)
    CafeUniTheme(darkTheme = false) {
        DashboardScreen(
            sessionManager = session,
            onLogout = {}
        )
    }
}
