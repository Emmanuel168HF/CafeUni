package com.cafeuni.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para la tabla "product".
 * Conserva la restricción require del tutorial (máximo 20 caracteres en el nombre).
 */
@Entity(tableName = "product")
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String
) {
    init {
        require(nombre.length <= 20) {
            "El nombre del producto no puede exceder los 20 caracteres"
        }
    }
}
