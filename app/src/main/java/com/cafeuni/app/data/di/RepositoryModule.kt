package com.cafeuni.app.data.di

import com.cafeuni.app.data.repository.ProductoRepositoryImp
import com.cafeuni.app.domain.repository.ProductoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Hilt para enlazar la interfaz del repositorio con su implementación.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductoRepository(
        productoRepositoryImp: ProductoRepositoryImp
    ): ProductoRepository
}
