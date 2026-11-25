package com.cafeuni.app.screens

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cafeuni.app.R
import com.cafeuni.app.components.CustomTextField
import com.cafeuni.app.data.repository.UsuarioRepository
import com.cafeuni.app.session.SessionManager
import kotlinx.coroutines.launch

/**
 * Pantalla 1: Inicio de Sesión (LoginScreen) construida con Material Design 3 (M3).
 *
 * Autenticación real con SQLite + PBKDF2:
 * - Valida correo institucional (@uttt.edu.mx).
 * - Verifica el hash PBKDF2 en la base de datos SQLite "CafeUni.db".
 * - Si es válido, guarda la sesión en DataStore y navega al Dashboard.
 */
@Composable
fun LoginScreen(
    usuarioRepository: UsuarioRepository,
    sessionManager: SessionManager,
    onNavigateToRegister: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    @StringRes var emailErrorResId by remember { mutableStateOf<Int?>(null) }
    @StringRes var passwordErrorResId by remember { mutableStateOf<Int?>(null) }

    val requiredDomain = "@uttt.edu.mx"

    fun validateAndSubmit() {
        var isValid = true

        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            emailErrorResId = R.string.error_email_required
            isValid = false
        } else if (!trimmedEmail.contains("@")) {
            emailErrorResId = R.string.error_invalid_email
            isValid = false
        } else if (!trimmedEmail.endsWith(requiredDomain, ignoreCase = true)) {
            emailErrorResId = R.string.error_invalid_email_domain
            isValid = false
        } else {
            emailErrorResId = null
        }

        if (password.isEmpty()) {
            passwordErrorResId = R.string.error_password_required
            isValid = false
        } else {
            passwordErrorResId = null
        }

        if (isValid) {
            coroutineScope.launch {
                val result = usuarioRepository.iniciarSesion(
                    correo = email,
                    contrasena = password
                )

                if (result.isSuccess) {
                    val usuario = result.getOrThrow()
                    sessionManager.saveSession(
                        idUsuario = usuario.idUsuario,
                        nombre = usuario.nombre,
                        correo = usuario.correo
                    )
                    Toast.makeText(
                        context,
                        R.string.message_login_success,
                        Toast.LENGTH_SHORT
                    ).show()
                    onNavigateToDashboard()
                } else {
                    val exception = result.exceptionOrNull()
                    when (exception?.message) {
                        "USUARIO_NO_ENCONTRADO" -> {
                            emailErrorResId = R.string.error_user_not_found
                        }
                        "CONTRASENA_INCORRECTA" -> {
                            passwordErrorResId = R.string.error_invalid_password
                        }
                        else -> {
                            Toast.makeText(
                                context,
                                exception?.message ?: "Error al iniciar sesión",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo oficial de Café Uni
            Image(
                painter = painterResource(id = R.drawable.logo_cafe_uni),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(170.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta de formulario elevada de Material Design 3
            ElevatedCard(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val emailErrorMessage = emailErrorResId?.let { stringResource(it) }
                    val passwordErrorMessage = passwordErrorResId?.let { stringResource(it) }

                    // Campo de correo institucional M3
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailErrorResId != null) emailErrorResId = null
                        },
                        label = stringResource(R.string.login_email),
                        placeholder = stringResource(R.string.login_email_placeholder),
                        leadingIcon = Icons.Default.Email,
                        isError = emailErrorResId != null,
                        errorMessage = emailErrorMessage,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo de contraseña M3 con alternador de visibilidad
                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordErrorResId != null) passwordErrorResId = null
                        },
                        label = stringResource(R.string.login_password),
                        placeholder = stringResource(R.string.login_password_placeholder),
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(R.string.login_toggle_password),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        isError = passwordErrorResId != null,
                        errorMessage = passwordErrorMessage,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón principal de Iniciar Sesión M3
                    Button(
                        onClick = { validateAndSubmit() },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login_button),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enlace M3 a la pantalla de Registro
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = stringResource(R.string.login_register),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
