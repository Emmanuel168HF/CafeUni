package com.cafeuni.app.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modelo de catálogo de productos para la tienda de Café Uni con código QR propio.
 */
data class ProductoCatalog(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val icon: ImageVector,
    val imageUrl: String = "",
    val disponible: Boolean = true
) {
    val precioFormateado: String
        get() = "$${"%.2f".format(precio)}"

    val codigoQr: String
        get() = "PROD-CAFEEUNI-${id}-${nombre.uppercase().replace(" ", "-")}"
}

/**
 * Elemento del carrito de compras.
 */
data class CartItem(
    val producto: ProductoCatalog,
    var cantidad: Int = 1
) {
    val subtotal: Double
        get() = producto.precio * cantidad

    val subtotalFormateado: String
        get() = "$${"%.2f".format(subtotal)}"
}

/**
 * Catálogo oficial de productos con imágenes reales de alta calidad y códigos QR únicos.
 */
object CatalogData {
    val productos = listOf(
        ProductoCatalog(
            id = 1,
            nombre = "Espresso Americano",
            descripcion = "Café negro tradicional preparado con granos de altura recién molidos.",
            precio = 35.0,
            categoria = "Bebidas",
            icon = Icons.Default.LocalCafe,
            imageUrl = "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=500&q=80"
        ),
        ProductoCatalog(
            id = 2,
            nombre = "Latte Vainilla",
            descripcion = "Espresso suave con leche cremosa al vapor y toque de vainilla.",
            precio = 48.0,
            categoria = "Bebidas",
            icon = Icons.Default.Coffee,
            imageUrl = "https://images.unsplash.com/photo-1570968915860-54d5c301fa9f?w=500&q=80"
        ),
        ProductoCatalog(
            id = 3,
            nombre = "Capuchino Caramelo",
            descripcion = "Capuchino clásico con abundante espuma de leche y sirope de caramelo.",
            precio = 45.0,
            categoria = "Bebidas",
            icon = Icons.Default.Coffee,
            imageUrl = "https://images.unsplash.com/photo-1534778101976-62847782c213?w=500&q=80"
        ),
        ProductoCatalog(
            id = 4,
            nombre = "Moka Frío con Crema",
            descripcion = "Café helado con chocolate, leche al gusto y crema batida.",
            precio = 52.0,
            categoria = "Bebidas",
            icon = Icons.Default.EmojiFoodBeverage,
            imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=500&q=80"
        ),
        ProductoCatalog(
            id = 5,
            nombre = "Frappé Choco-Café",
            descripcion = "Bebida helada frapada de café con chispas de chocolate.",
            precio = 55.0,
            categoria = "Bebidas",
            icon = Icons.Default.Icecream,
            imageUrl = "https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=500&q=80"
        ),
        ProductoCatalog(
            id = 6,
            nombre = "Té Verde con Miel",
            descripcion = "Infusión de té verde orgánico endulzado con miel de abeja.",
            precio = 38.0,
            categoria = "Bebidas",
            icon = Icons.Default.EmojiFoodBeverage,
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=500&q=80"
        ),
        ProductoCatalog(
            id = 7,
            nombre = "Baguette Pavo y Queso",
            descripcion = "Baguette crujiente con pechuga de pavo, queso manchego y aderezo.",
            precio = 65.0,
            categoria = "Comida",
            icon = Icons.Default.LunchDining,
            imageUrl = "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=500&q=80"
        ),
        ProductoCatalog(
            id = 8,
            nombre = "Croissant de Jamón",
            descripcion = "Croissant hojaldrado horneado relleno de jamón y queso.",
            precio = 32.0,
            categoria = "Comida",
            icon = Icons.Default.BakeryDining,
            imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=500&q=80",
            disponible = false
        ),
        ProductoCatalog(
            id = 9,
            nombre = "Sándwich Pollo Chipotle",
            descripcion = "Pan artesanal con pechuga de pollo y aderezo especial de chipotle.",
            precio = 58.0,
            categoria = "Comida",
            icon = Icons.Default.Restaurant,
            imageUrl = "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=500&q=80"
        ),
        ProductoCatalog(
            id = 10,
            nombre = "Pastel de Chocolate",
            descripcion = "Rebanada de pastel húmedo de chocolate con cobertura fudgy.",
            precio = 42.0,
            categoria = "Postres",
            icon = Icons.Default.Cake,
            imageUrl = "https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=500&q=80"
        ),
        ProductoCatalog(
            id = 11,
            nombre = "Cheesecake Frutos Rojos",
            descripcion = "Pastel de queso crema artesanal con mermelada de frutos rojos.",
            precio = 48.0,
            categoria = "Postres",
            icon = Icons.Default.Cake,
            imageUrl = "https://images.unsplash.com/photo-1533134242443-d4fd215305ad?w=500&q=80"
        )
    )
}
