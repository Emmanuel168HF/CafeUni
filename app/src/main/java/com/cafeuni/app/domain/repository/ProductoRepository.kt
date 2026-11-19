package com.cafeuni.app.domain.repository

import com.cafeuni.app.domain.model.Producto
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de productos en la capa de dominio.
 */
interface ProductoRepository {
    fun obtenerProductos(): Flow<List<Producto>>
    suspend fun obtenerProductoPorId(id: Int): Producto?
    suspend fun insertarProducto(producto: Producto)
    suspend fun actualizarProducto(producto: Producto)
    suspend fun eliminarProducto(producto: Producto)
}
