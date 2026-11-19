package com.cafeuni.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cafeuni.app.data.database.dao.ProductoDao
import com.cafeuni.app.data.database.entity.ProductoEntity

/**
 * Base de datos Room principal para Café Uni.
 */
@Database(
    entities = [ProductoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao
}
