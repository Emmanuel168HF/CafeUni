package com.cafeuni.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cafeuni.app.domain.model.Producto
import com.cafeuni.app.domain.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel inyectado por Hilt para la gestión de productos.
 *
 * NOTA: Esta clase forma parte de la infraestructura creada de ejemplo según el tutorial.
 * Puede inyectarse más adelante en pantallas con: `val viewModel: ProductoViewModel = hiltViewModel()`
 *
 * Para crear nuevas entidades (p. ej. Usuarios, Pedidos, Categorías):
 * 1. Crear Entity en data/database/entity/
 * 2. Crear Dao en data/database/dao/ e incluirlo en AppDatabase
 * 3. Crear Model en domain/model/ y Repository en domain/repository/
 * 4. Implementar RepositoryImp en data/repository/ y vincular en RepositoryModule
 * 5. Crear el ViewModel correspondiente inyectando el Repositorio
 */
@HiltViewModel
class ProductoViewModel @Inject constructor(
    private val repository: ProductoRepository
) : ViewModel() {

    val productos: StateFlow<List<Producto>> = repository.obtenerProductos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarProducto(nombre: String, descripcion: String) {
        viewModelScope.launch {
            repository.insertarProducto(Producto(nombre = nombre, descripcion = descripcion))
        }
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.actualizarProducto(producto)
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.eliminarProducto(producto)
        }
    }
}
