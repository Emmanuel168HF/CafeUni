package com.cafeuni.app.screens

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cafeuni.app.R
import com.cafeuni.app.components.CustomTextField

/**
 * Pantalla 2: Registro (RegisterScreen) construida con Material Design 3 (M3).
 *
 * Características M3:
 * - `ElevatedCard` con contenedor de superficie M3 y bordes redondeados (`extraLarge`).
 * - `FilledTonalButton` para acciones secundarias M3.
 * - Formularios con tipografías y validaciones M3.
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    @StringRes var nameErrorResId by remember { mutableStateOf<Int?>(null) }
    @StringRes var lastnameErrorResId by remember { mutableStateOf<Int?>(null) }
    @StringRes var emailErrorResId by remember { mutableStateOf<Int?>(null) }
    @StringRes var passwordErrorResId by remember { mutableStateOf<Int?>(null) }
    @StringRes var confirmPasswordErrorResId by remember { mutableStateOf<Int?>(null) }

    val requiredDomain = "@uttt.edu.mx"

    fun validateAndSubmit() {
        var isValid = true

        if (name.trim().isEmpty()) {
            nameErrorResId = R.string.error_name_required
            isValid = false
        } else {
            nameErrorResId = null
        }

        if (lastname.trim().isEmpty()) {
            lastnameErrorResId = R.string.error_lastname_required
            isValid = false
        } else {
            lastnameErrorResId = null
        }

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

        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorResId = R.string.error_password_required
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordErrorResId = R.string.error_password_mismatch
            isValid = false
        } else {
            confirmPasswordErrorResId = null
        }

        if (isValid) {
            Toast.makeText(
                context,
                R.string.message_register_not_implemented,
                Toast.LENGTH_LONG
            ).show()
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tarjeta de registro elevada M3
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
                    val nameErrorMessage = nameErrorResId?.let { stringResource(it) }
                    val lastnameErrorMessage = lastnameErrorResId?.let { stringResource(it) }
                    val emailErrorMessage = emailErrorResId?.let { stringResource(it) }
                    val passwordErrorMessage = passwordErrorResId?.let { stringResource(it) }
                    val confirmPasswordErrorMessage = confirmPasswordErrorResId?.let { stringResource(it) }

                    // Campo Nombre(s)
                    CustomTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameErrorResId != null) nameErrorResId = null
                        },
                        label = stringResource(R.string.register_name),
                        placeholder = stringResource(R.string.register_name_placeholder),
                        leadingIcon = Icons.Default.Person,
                        isError = nameErrorResId != null,
                        errorMessage = nameErrorMessage
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Apellido(s)
                    CustomTextField(
                        value = lastname,
                        onValueChange = {
                            lastname = it
                            if (lastnameErrorResId != null) lastnameErrorResId = null
                        },
                        label = stringResource(R.string.register_lastname),
                        placeholder = stringResource(R.string.register_lastname_placeholder),
                        leadingIcon = Icons.Default.Person,
                        isError = lastnameErrorResId != null,
                        errorMessage = lastnameErrorMessage
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Correo institucional
                    CustomTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailErrorResId != null) emailErrorResId = null
                        },
                        label = stringResource(R.string.register_email),
                        placeholder = stringResource(R.string.register_email_placeholder),
                        leadingIcon = Icons.Default.Email,
                        isError = emailErrorResId != null,
                        errorMessage = emailErrorMessage,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Contraseña
                    CustomTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordErrorResId != null) passwordErrorResId = null
                        },
                        label = stringResource(R.string.register_password),
                        placeholder = stringResource(R.string.register_password_placeholder),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Confirmar Contraseña
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmPasswordErrorResId != null) confirmPasswordErrorResId = null
                        },
                        label = stringResource(R.string.register_confirm_password),
                        placeholder = stringResource(R.string.register_confirm_password_placeholder),
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = stringResource(R.string.login_toggle_password),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        isError = confirmPasswordErrorResId != null,
                        errorMessage = confirmPasswordErrorMessage,
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón "Crear cuenta" M3
                    Button(
                        onClick = { validateAndSubmit() },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.register_button),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enlace M3 para volver a Iniciar Sesión
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.register_has_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.register_login),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón secundario M3 (FilledTonalButton) para ver el Dashboard directamente
            FilledTonalButton(
                onClick = onNavigateToDashboard,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.register_temporary_dashboard),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
