package com.cafeuni.app.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

/**
 * Gestor de sesiones de usuario utilizando DataStore Preferences.
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    }

    /**
     * Flujo que emite el ID del usuario con sesión activa o null si no hay sesión.
     */
    val userIdFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }

    /**
     * Flujo que emite el nombre del usuario con sesión activa o cadena vacía.
     */
    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: ""
    }

    /**
     * Flujo que emite la URI de la foto de perfil guardada o null.
     */
    val profileImageUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PROFILE_IMAGE_URI]
    }

    /**
     * Flujo que emite true si el usuario ha iniciado sesión.
     */
    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ID] != null
    }

    /**
     * Guarda los datos del usuario autenticado en DataStore.
     */
    suspend fun saveSession(idUsuario: Long, nombre: String, correo: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = idUsuario
            preferences[KEY_USER_NAME] = nombre
            preferences[KEY_USER_EMAIL] = correo
        }
    }

    /**
     * Guarda la URI de la foto de perfil en DataStore.
     */
    suspend fun saveProfileImageUri(uriString: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PROFILE_IMAGE_URI] = uriString
        }
    }

    /**
     * Elimina la sesión activa de DataStore al cerrar sesión.
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_PROFILE_IMAGE_URI)
        }
    }
}
