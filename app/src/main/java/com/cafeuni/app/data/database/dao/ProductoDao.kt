package com.cafeuni.app.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cafeuni.app.data.database.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para realizar operaciones en la tabla "product".
 */
@Dao
interface ProductoDao {

    @Query("SELECT * FROM product ORDER BY id DESC")
    fun obtenerProductos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM product WHERE id = :id LIMIT 1")
    suspend fun getItem(id: Int): ProductoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarProducto(producto: ProductoEntity)

    @Update
    suspend fun actualizarProducto(producto: ProductoEntity)

    @Delete
    suspend fun eliminarProducto(producto: ProductoEntity)
}
