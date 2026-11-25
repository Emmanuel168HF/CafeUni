package com.cafeuni.app.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.cafeuni.app.R
import com.cafeuni.app.components.AboutDialog
import com.cafeuni.app.components.CartDialog
import com.cafeuni.app.components.QrScannerDialog
import com.cafeuni.app.components.SecurityInfoDialog
import com.cafeuni.app.components.TicketQrDialog
import com.cafeuni.app.data.dao.PedidoDao
import com.cafeuni.app.data.dao.PedidoRegistroModel
import com.cafeuni.app.data.repository.UsuarioRepository
import com.cafeuni.app.models.CartItem
import com.cafeuni.app.models.CatalogData
import com.cafeuni.app.models.ProductoCatalog
import com.cafeuni.app.models.Usuario
import com.cafeuni.app.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla 3: Dashboard Principal (DashboardScreen)
 *
 * Funcionalidades completadas:
 * 1. Pestaña Inicio con catálogo interactivo, filtro por categorías y buscador en tiempo real.
 * 2. Carrito de Compras funcional con diálogo interactivo, ajuste de cantidades e inserción real en SQLite.
 * 3. Pestaña Pedidos conectada a SQLite mostrando el historial de pedidos y tickets con QR.
 * 4. Pestaña Perfil terminada: datos personales de SQLite, foto de perfil persistente, configuración de notificaciones, diálogo de seguridad y diálogo Acerca de.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    sessionManager: SessionManager,
    usuarioRepository: UsuarioRepository,
    pedidoDao: PedidoDao,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userName by sessionManager.userNameFlow.collectAsStateWithLifecycle(initialValue = "")
    val userId by sessionManager.userIdFlow.collectAsStateWithLifecycle(initialValue = null)
    val profileImageUri by sessionManager.profileImageUriFlow.collectAsStateWithLifecycle(initialValue = null)

    var currentUser by remember { mutableStateOf<Usuario?>(null) }
    var selectedTab by remember { mutableStateOf("inicio") }

    // Estado del Carrito de compras
    val cartItems = remember { mutableStateListOf<CartItem>() }

    // Estados de Diálogos
    var showCartDialog by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Estado de Ticket activo generado
    var activeTicketData by remember { mutableStateOf<Triple<String, String, String>?>(null) } // Triple(ProductoNombre, Precio, CodigoQR)
    var searchQuery by remember { mutableStateOf("") }

    // Lista de pedidos reales consultados desde SQLite
    var historialPedidos by remember { mutableStateOf<List<PedidoRegistroModel>>(emptyList()) }

    // Carga los datos del usuario e historial de pedidos desde SQLite
    LaunchedEffect(userId, selectedTab) {
        userId?.let { id ->
            withContext(Dispatchers.IO) {
                val user = usuarioRepository.obtenerUsuarioPorId(id)
                val pedidos = pedidoDao.obtenerPedidosPorUsuario(id)
                withContext(Dispatchers.Main) {
                    currentUser = user
                    historialPedidos = pedidos
                }
            }
        }
    }

    fun handleLogout() {
        coroutineScope.launch {
            sessionManager.clearSession()
            onLogout()
        }
    }

    fun handleAddToCart(product: ProductoCatalog) {
        val existingIndex = cartItems.indexOfFirst { it.producto.id == product.id }
        if (existingIndex >= 0) {
            cartItems[existingIndex].cantidad += 1
        } else {
            cartItems.add(CartItem(producto = product, cantidad = 1))
        }
        Toast.makeText(context, "${product.nombre} agregado al carrito", Toast.LENGTH_SHORT).show()
    }

    fun handleConfirmOrderFromCart() {
        if (cartItems.isEmpty()) return
        val total = cartItems.sumOf { it.subtotal }
        val totalFormatted = "$${"%.2f".format(total)}"
        val resumen = cartItems.joinToString(", ") { "${it.cantidad}x ${it.producto.nombre}" }

        coroutineScope.launch(Dispatchers.IO) {
            val ticketResult = pedidoDao.crearPedidoMultipleConTicket(
                idUsuario = userId ?: 1L,
                items = cartItems.toList(),
                total = total
            )
            val nuevosPedidos = pedidoDao.obtenerPedidosPorUsuario(userId ?: 1L)

            withContext(Dispatchers.Main) {
                cartItems.clear()
                showCartDialog = false
                historialPedidos = nuevosPedidos
                activeTicketData = Triple(resumen, totalFormatted, ticketResult.codigoQr)
            }
        }
    }

    // Diálogos Modales
    if (showCartDialog) {
        CartDialog(
            cartItems = cartItems,
            onIncrementQuantity = { item ->
                val index = cartItems.indexOf(item)
                if (index >= 0) cartItems[index] = item.copy(cantidad = item.cantidad + 1)
            },
            onDecrementQuantity = { item ->
                val index = cartItems.indexOf(item)
                if (index >= 0) {
                    if (item.cantidad > 1) {
                        cartItems[index] = item.copy(cantidad = item.cantidad - 1)
                    } else {
                        cartItems.removeAt(index)
                    }
                }
            },
            onRemoveItem = { item -> cartItems.remove(item) },
            onConfirmOrder = { handleConfirmOrderFromCart() },
            onDismiss = { showCartDialog = false }
        )
    }

    if (showQrScanner) {
        QrScannerDialog(
            onDismiss = { showQrScanner = false },
            onQrScanned = { scannedCode ->
                searchQuery = scannedCode
                Toast.makeText(context, "Menú escaneado: $scannedCode", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showSecurityDialog) {
        SecurityInfoDialog(onDismiss = { showSecurityDialog = false })
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (activeTicketData != null) {
        TicketQrDialog(
            productoNombre = activeTicketData!!.first,
            precio = activeTicketData!!.second,
            codigoQr = activeTicketData!!.third,
            onDismiss = { activeTicketData = null }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!profileImageUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = Uri.parse(profileImageUri),
                                        contentDescription = "Foto de Perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Coffee,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.dashboard_header_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.dashboard_header_location),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Botón para escanear QR del Menú Físico
                    IconButton(onClick = { showQrScanner = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Escanear QR de Menú Físico",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Botón funcional de Cerrar Sesión
                    IconButton(onClick = { handleLogout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.dashboard_logout),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    selected = selectedTab == "inicio",
                    onClick = { selectedTab = "inicio" },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.dashboard_nav_home)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "pedidos",
                    onClick = { selectedTab = "pedidos" },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                    label = { Text(stringResource(R.string.dashboard_nav_orders)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "perfil",
                    onClick = { selectedTab = "perfil" },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.dashboard_nav_profile)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                "inicio" -> DashboardInicioTabContent(
                    userName = userName,
                    searchQuery = searchQuery,
                    cartCount = cartItems.sumOf { it.cantidad },
                    onSearchQueryChange = { searchQuery = it },
                    onOpenScanner = { showQrScanner = true },
                    onOpenCart = { showCartDialog = true },
                    onAddToCart = { product -> handleAddToCart(product) }
                )
                "pedidos" -> DashboardPedidosTabContent(
                    pedidos = historialPedidos,
                    onViewTicket = { nota, qr -> activeTicketData = Triple(nota, "", qr) }
                )
                "perfil" -> DashboardPerfilTabContent(
                    usuario = currentUser,
                    userNameFromSession = userName,
                    profileImageUri = profileImageUri,
                    notificationsEnabled = notificationsEnabled,
                    onToggleNotifications = { notificationsEnabled = it },
                    onOpenSecurityInfo = { showSecurityDialog = true },
                    onOpenAboutInfo = { showAboutDialog = true },
                    onUpdateProfileImage = { uriStr ->
                        coroutineScope.launch {
                            sessionManager.saveProfileImageUri(uriStr)
                        }
                    },
                    onLogout = { handleLogout() }
                )
            }
        }
    }
}

/**
 * Pestaña 1: Inicio (Con catálogo de productos, filtros y carrito)
 */
@Composable
private fun DashboardInicioTabContent(
    userName: String,
    searchQuery: String,
    cartCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onOpenCart: () -> Unit,
    onAddToCart: (ProductoCatalog) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todo") }

    val categories = listOf("Todo", "Bebidas", "Comida", "Postres")
    val displayName = if (userName.isNotEmpty()) userName else "Sofía"

    // Filtro dinámico de productos por categoría y buscador
    val productosFiltrados = remember(selectedCategory, searchQuery) {
        CatalogData.productos.filter { prod ->
            val matchesCategory = (selectedCategory == "Todo" || prod.categoria.equals(selectedCategory, ignoreCase = true))
            val matchesSearch = searchQuery.isBlank() ||
                    prod.nombre.contains(searchQuery, ignoreCase = true) ||
                    prod.descripcion.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 1. Saludo e indicador de Carrito con Contador
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.dashboard_user_greeting, displayName),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.dashboard_greeting_question),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        AssistChip(
            onClick = onOpenCart,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (cartCount > 0) "Carrito ($cartCount)" else stringResource(R.string.dashboard_cart_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            shape = CircleShape
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Campo de Búsqueda con botón de Escáner QR
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(
                text = stringResource(R.string.dashboard_search_hint),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(onClick = onOpenScanner) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Escanear Menú Físico",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        shape = CircleShape,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    // 3. Fila de Filtros/Categorías
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { selectedCategory = category },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                shape = CircleShape
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 4. Banner "BENEFICIO CAMPUS - 15% de descuento"
    ElevatedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_banner_tag),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.dashboard_banner_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = { },
                shape = CircleShape
            ) {
                Text(
                    text = stringResource(R.string.dashboard_banner_action),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 5. Encabezado de Productos
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Catálogo de la Cafetería",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${productosFiltrados.size} opciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 6. Lista/Cuadrícula de productos filtrados
    if (productosFiltrados.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No se encontraron productos para \"$searchQuery\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            productosFiltrados.chunked(2).forEach { par ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val prod1 = par[0]
                    ProductCatalogCard(
                        producto = prod1,
                        onAddToCart = { onAddToCart(prod1) },
                        modifier = Modifier.weight(1f)
                    )
                    if (par.size > 1) {
                        val prod2 = par[1]
                        ProductCatalogCard(
                            producto = prod2,
                            onAddToCart = { onAddToCart(prod2) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // 7. Banner "Recoger en taquilla rápida"
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.dashboard_pickup_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.dashboard_pickup_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

/**
 * Tarjeta interactiva de producto del catálogo.
 */
@Composable
private fun ProductCatalogCard(
    producto: ProductoCatalog,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = producto.icon,
                    contentDescription = producto.nombre,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = CircleShape,
                color = if (producto.disponible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = if (producto.disponible) "Disponible" else "Agotado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.disponible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = producto.precioFormateado,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onAddToCart,
                    enabled = producto.disponible,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar al carrito",
                        tint = if (producto.disponible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

/**
 * Pestaña 2: Pedidos (Conectada a SQLite con historial real)
 */
@Composable
private fun DashboardPedidosTabContent(
    pedidos: List<PedidoRegistroModel>,
    onViewTicket: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.orders_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.orders_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (pedidos.isEmpty()) {
            ElevatedCard(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.orders_placeholder),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.orders_placeholder_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pedidos.forEach { item ->
                    OutlinedCard(
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pedido #${item.idPedido}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = item.estadoPedido,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Fecha: ${item.fechaPedido}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = item.notaPedido,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onViewTicket(item.notaPedido, item.codigoQr) },
                                shape = CircleShape,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver Código QR del Ticket")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Pestaña 3: Perfil (Terminada con foto, datos SQLite y Configuración)
 */
@Composable
private fun DashboardPerfilTabContent(
    usuario: Usuario?,
    userNameFromSession: String,
    profileImageUri: String?,
    notificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onOpenSecurityInfo: () -> Unit,
    onOpenAboutInfo: () -> Unit,
    onUpdateProfileImage: (String) -> Unit,
    onLogout: () -> Unit
) {
    val nombreCompleto = usuario?.let { "${it.nombre} ${it.apellidos}" } ?: userNameFromSession.ifEmpty { "Usuario UTTT" }
    val correo = usuario?.correo ?: "usuario@uttt.edu.mx"
    val idUsuario = usuario?.idUsuario?.toString() ?: "-"

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onUpdateProfileImage(it.toString())
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(R.string.profile_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Tarjeta principal del usuario con foto de perfil
        ElevatedCard(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable {
                        photoPickerLauncher.launch("image/*")
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!profileImageUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = Uri.parse(profileImageUri),
                                    contentDescription = "Foto de Perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Cambiar Foto",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreCompleto,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Tarjeta con datos personales recuperados de SQLite
        Text(
            text = stringResource(R.string.profile_personal_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailRow(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.profile_name_label),
                    value = nombreCompleto
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailRow(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.profile_email_label),
                    value = correo
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailRow(
                    icon = Icons.Default.Badge,
                    label = stringResource(R.string.profile_id_label),
                    value = "#$idUsuario"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileDetailRow(
                    icon = Icons.Default.School,
                    label = stringResource(R.string.profile_institution_label),
                    value = stringResource(R.string.profile_institution_name)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Sección de Ajustes y Configuración Terminada
        Text(
            text = stringResource(R.string.profile_settings_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedCard(
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_option_notifications),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onToggleNotifications
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSecurityInfo() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_option_security),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAboutInfo() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_option_about),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Botón de Cerrar Sesión en Perfil
        Button(
            onClick = onLogout,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.dashboard_logout),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
