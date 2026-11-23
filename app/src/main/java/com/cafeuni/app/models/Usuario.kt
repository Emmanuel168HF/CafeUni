package com.cafeuni.app.models

/**
 * Modelo de datos para la entidad Usuario correspondiente a la tabla SQL `Usuario`.
 */
data class Usuario(
    val idUsuario: Long = 0,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    val contrasena: String
)
