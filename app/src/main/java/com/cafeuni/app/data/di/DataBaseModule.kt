package com.cafeuni.app.data.di

import android.content.Context
import androidx.room.Room
import com.cafeuni.app.data.database.AppDatabase
import com.cafeuni.app.data.database.dao.ProductoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para la provisión de Room y DAOs.
 * La base de datos se denomina "cafeuni.db".
 */
@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cafeuni.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideProductoDao(appDatabase: AppDatabase): ProductoDao {
        return appDatabase.productoDao()
    }
}
