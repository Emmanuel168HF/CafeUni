package com.cafeuni.app.data.mapper

import com.cafeuni.app.data.database.entity.ProductoEntity
import com.cafeuni.app.domain.model.Producto

/**
 * Mapper de conversión entre la entidad de base de datos (ProductoEntity)
 * y el modelo de dominio (Producto).
 */
fun ProductoEntity.toDomain(): Producto {
    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion
    )
}

fun Producto.toEntity(): ProductoEntity {
    return ProductoEntity(
        id = id,
        nombre = nombre,
        descripcion = descripcion
    )
}
