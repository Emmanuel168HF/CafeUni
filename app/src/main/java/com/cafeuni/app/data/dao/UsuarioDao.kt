package com.cafeuni.app.data.dao

import android.content.ContentValues
import com.cafeuni.app.data.database.DatabaseHelper
import com.cafeuni.app.models.Usuario
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data Access Object (DAO) para realizar operaciones CRUD nativas sobre la tabla `Usuario`.
 */
@Singleton
class UsuarioDao @Inject constructor(
    private val dbHelper: DatabaseHelper
) {

    /**
     * Inserta un nuevo usuario en la tabla Usuario y retorna su id_usuario generado.
     */
    fun insertarUsuario(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("nombre", usuario.nombre)
            put("apellidos", usuario.apellidos)
            put("correo", usuario.correo.lowercase().trim())
            put("contrasena", usuario.contrasena)
        }
        return db.insert("Usuario", null, values)
    }

    /**
     * Consulta un usuario por su correo electrónico.
     */
    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "Usuario",
            arrayOf("id_usuario", "nombre", "apellidos", "correo", "contrasena"),
            "LOWER(correo) = ?",
            arrayOf(correo.lowercase().trim()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Usuario(
                    idUsuario = it.getLong(it.getColumnIndexOrThrow("id_usuario")),
                    nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                    apellidos = it.getString(it.getColumnIndexOrThrow("apellidos")),
                    correo = it.getString(it.getColumnIndexOrThrow("correo")),
                    contrasena = it.getString(it.getColumnIndexOrThrow("contrasena"))
                )
            } else {
                null
            }
        }
    }

    /**
     * Consulta un usuario por su identificador primario `id_usuario`.
     */
    fun obtenerUsuarioPorId(idUsuario: Long): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "Usuario",
            arrayOf("id_usuario", "nombre", "apellidos", "correo", "contrasena"),
            "id_usuario = ?",
            arrayOf(idUsuario.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Usuario(
                    idUsuario = it.getLong(it.getColumnIndexOrThrow("id_usuario")),
                    nombre = it.getString(it.getColumnIndexOrThrow("nombre")),
                    apellidos = it.getString(it.getColumnIndexOrThrow("apellidos")),
                    correo = it.getString(it.getColumnIndexOrThrow("correo")),
                    contrasena = it.getString(it.getColumnIndexOrThrow("contrasena"))
                )
            } else {
                null
            }
        }
    }

    /**
     * Comprueba si un correo ya se encuentra registrado.
     */
    fun existeCorreo(correo: String): Boolean {
        return obtenerUsuarioPorCorreo(correo) != null
    }
}
