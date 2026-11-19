package com.cafeuni.app.data.repository

import com.cafeuni.app.data.database.dao.ProductoDao
import com.cafeuni.app.data.mapper.toDomain
import com.cafeuni.app.data.mapper.toEntity
import com.cafeuni.app.domain.model.Producto
import com.cafeuni.app.domain.repository.ProductoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de productos inyectado con Hilt.
 */
@Singleton
class ProductoRepositoryImp @Inject constructor(
    private val productoDao: ProductoDao
) : ProductoRepository {

    override fun obtenerProductos(): Flow<List<Producto>> {
        return productoDao.obtenerProductos().map { lista ->
            lista.map { it.toDomain() }
        }
    }

    override suspend fun obtenerProductoPorId(id: Int): Producto? {
        return productoDao.getItem(id)?.toDomain()
    }

    override suspend fun insertarProducto(producto: Producto) {
        productoDao.insertarProducto(producto.toEntity())
    }

    override suspend fun actualizarProducto(producto: Producto) {
        productoDao.actualizarProducto(producto.toEntity())
    }

    override suspend fun eliminarProducto(producto: Producto) {
        productoDao.eliminarProducto(producto.toEntity())
    }
}
