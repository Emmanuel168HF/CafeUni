package com.cafeuni.app.domain.model

/**
 * Modelo de dominio para representar un Producto.
 */
data class Producto(
    val id: Int = 0,
    val nombre: String,
    val descripcion: String
)
