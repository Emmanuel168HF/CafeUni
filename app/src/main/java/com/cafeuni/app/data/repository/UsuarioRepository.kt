package com.cafeuni.app.data.repository

import com.cafeuni.app.data.dao.UsuarioDao
import com.cafeuni.app.data.security.PasswordHasher
import com.cafeuni.app.models.Usuario
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio de la entidad Usuario para la administración de registros e inicios de sesión reales.
 */
@Singleton
class UsuarioRepository @Inject constructor(
    private val usuarioDao: UsuarioDao,
    private val passwordHasher: PasswordHasher
) {

    /**
     * Registra un nuevo usuario verificando que no exista el correo y aplicando hash PBKDF2 a la contraseña.
     */
    fun registrarUsuario(
        nombre: String,
        apellidos: String,
        correo: String,
        contrasena: String
    ): Result<Long> {
        val trimmedCorreo = correo.trim().lowercase()

        if (usuarioDao.existeCorreo(trimmedCorreo)) {
            return Result.failure(IllegalStateException("EL_CORREO_YA_EXISTE"))
        }

        val hashedPassword = passwordHasher.hashPassword(contrasena)
        val nuevoUsuario = Usuario(
            nombre = nombre.trim(),
            apellidos = apellidos.trim(),
            correo = trimmedCorreo,
            contrasena = hashedPassword
        )

        val idGenerado = usuarioDao.insertarUsuario(nuevoUsuario)
        return if (idGenerado > 0) {
            Result.success(idGenerado)
        } else {
            Result.failure(RuntimeException("Error al insertar el usuario en la base de datos"))
        }
    }

    /**
     * Valida el inicio de sesión comparando la contraseña mediante el hash PBKDF2 almacenado.
     */
    fun iniciarSesion(correo: String, contrasena: String): Result<Usuario> {
        val trimmedCorreo = correo.trim().lowercase()
        val usuario = usuarioDao.obtenerUsuarioPorCorreo(trimmedCorreo)
            ?: return Result.failure(IllegalArgumentException("USUARIO_NO_ENCONTRADO"))

        val isPasswordValid = passwordHasher.verifyPassword(contrasena, usuario.contrasena)
        return if (isPasswordValid) {
            Result.success(usuario)
        } else {
            Result.failure(IllegalArgumentException("CONTRASENA_INCORRECTA"))
        }
    }

    /**
     * Obtiene el usuario por su identificador primario `id_usuario`.
     */
    fun obtenerUsuarioPorId(idUsuario: Long): Usuario? {
        return usuarioDao.obtenerUsuarioPorId(idUsuario)
    }
}
