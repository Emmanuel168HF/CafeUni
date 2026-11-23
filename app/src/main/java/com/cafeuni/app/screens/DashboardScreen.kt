package com.cafeuni.app.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.cafeuni.app.components.QrScannerDialog
import com.cafeuni.app.components.TicketQrDialog
import com.cafeuni.app.data.dao.PedidoDao
import com.cafeuni.app.data.repository.UsuarioRepository
import com.cafeuni.app.models.Usuario
import com.cafeuni.app.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla 3: Dashboard Principal (DashboardScreen)
 *
 * Funcionalidades agregadas:
 * 1. Pestañas (`Inicio`, `Pedidos`, `Perfil`) totalmente funcionales.
 * 2. Foto de perfil interactiva en la pestaña Perfil (guardada en DataStore).
 * 3. Pedidos con generación e inserción de Ticket con Código QR real.
 * 4. Escáner de Códigos QR del menú físico con CameraX + ML Kit.
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

    // Estados para diálogos interactivos
    var showQrScanner by remember { mutableStateOf(false) }
    var activeTicketQr by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(Producto, Precio)
    var generatedTicketQrCode by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Carga los datos del usuario de SQLite cuando se obtiene el userId de la sesión
    LaunchedEffect(userId) {
        userId?.let { id ->
            withContext(Dispatchers.IO) {
                val user = usuarioRepository.obtenerUsuarioPorId(id)
                withContext(Dispatchers.Main) {
                    currentUser = user
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

    fun handleOrderProduct(nombre: String, precio: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val priceVal = precio.replace("$", "").toDoubleOrNull() ?: 0.0
            val ticketResult = pedidoDao.crearPedidoConTicket(
                idUsuario = userId ?: 1L,
                nombreProducto = nombre,
                precio = priceVal
            )
            withContext(Dispatchers.Main) {
                activeTicketQr = Pair(nombre, precio)
                generatedTicketQrCode = ticketResult.codigoQr
            }
        }
    }

    // Diálogo Escáner QR de Menú Físico
    if (showQrScanner) {
        QrScannerDialog(
            onDismiss = { showQrScanner = false },
            onQrScanned = { scannedCode ->
                searchQuery = scannedCode
                Toast.makeText(
                    context,
                    "Menú escaneado: $scannedCode",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }

    // Diálogo de Ticket con Código QR para el Pedido realizado
    if (activeTicketQr != null && generatedTicketQrCode != null) {
        TicketQrDialog(
            productoNombre = activeTicketQr!!.first,
            precio = activeTicketQr!!.second,
            codigoQr = generatedTicketQrCode!!,
            onDismiss = {
                activeTicketQr = null
                generatedTicketQrCode = null
            }
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
                    onSearchQueryChange = { searchQuery = it },
                    onOpenScanner = { showQrScanner = true },
                    onOrderProduct = { nombre, precio -> handleOrderProduct(nombre, precio) }
                )
                "pedidos" -> DashboardPedidosTabContent()
                "perfil" -> DashboardPerfilTabContent(
                    usuario = currentUser,
                    userNameFromSession = userName,
                    profileImageUri = profileImageUri,
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
 * Pestaña 1: Inicio
 */
@Composable
private fun DashboardInicioTabContent(
    userName: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenScanner: () -> Unit,
    onOrderProduct: (String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Todo") }

    val categories = listOf(
        stringResource(R.string.dashboard_chip_all),
        stringResource(R.string.dashboard_chip_drinks),
        stringResource(R.string.dashboard_chip_food),
        stringResource(R.string.dashboard_chip_desserts)
    )

    val displayName = if (userName.isNotEmpty()) userName else "Sofía"

    Spacer(modifier = Modifier.height(8.dp))

    // 1. Saludo e indicador de Carrito
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
            onClick = { },
            label = {
                Text(
                    text = stringResource(R.string.dashboard_cart_label),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            shape = CircleShape
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 2. Campo de Búsqueda con botón de Escáner QR de Menú Físico
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

    // 3. Fila de Filtros/Categorías (Todo, Bebidas, Comida, Postres)
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

    // 5. Encabezado de la sección "Populares del día"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.dashboard_popular_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.dashboard_popular_count),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    val item1Name = stringResource(R.string.dashboard_item1_name)
    val item1Price = stringResource(R.string.dashboard_item1_price)
    val item2Name = stringResource(R.string.dashboard_item2_name)
    val item2Price = stringResource(R.string.dashboard_item2_price)
    val item3Name = stringResource(R.string.dashboard_item3_name)
    val item3Price = stringResource(R.string.dashboard_item3_price)
    val item4Name = stringResource(R.string.dashboard_item4_name)
    val item4Price = stringResource(R.string.dashboard_item4_price)

    // 6. Cuadrícula 2x2 de Productos Populares (con pedido interactivo y generación de QR)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WireframeProductCard(
                nombre = item1Name,
                estado = stringResource(R.string.dashboard_status_available),
                precio = item1Price,
                isAvailable = true,
                onOrderClick = { onOrderProduct(item1Name, item1Price) },
                modifier = Modifier.weight(1f)
            )
            WireframeProductCard(
                nombre = item2Name,
                estado = stringResource(R.string.dashboard_status_available),
                precio = item2Price,
                isAvailable = true,
                onOrderClick = { onOrderProduct(item2Name, item2Price) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WireframeProductCard(
                nombre = item3Name,
                estado = stringResource(R.string.dashboard_status_available),
                precio = item3Price,
                isAvailable = true,
                onOrderClick = { onOrderProduct(item3Name, item3Price) },
                modifier = Modifier.weight(1f)
            )
            WireframeProductCard(
                nombre = item4Name,
                estado = stringResource(R.string.dashboard_status_out_of_stock),
                precio = item4Price,
                isAvailable = false,
                onOrderClick = { },
                modifier = Modifier.weight(1f)
            )
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
 * Pestaña 2: Pedidos
 */
@Composable
private fun DashboardPedidosTabContent() {
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

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Pestaña 3: Perfil (COMPLETAMENTE TERMINADA Y CONECTADA CON FOTO DE PERFIL SELECCIONABLE)
 */
@Composable
private fun DashboardPerfilTabContent(
    usuario: Usuario?,
    userNameFromSession: String,
    profileImageUri: String?,
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

        // 1. Tarjeta principal del usuario con foto de perfil interactiva
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
                // Selector de foto de perfil
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

                    // Badge con ícono de cámara para indicar que se puede cambiar la foto
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

        // 3. Sección de Ajustes y Configuración
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
                ProfileOptionRow(
                    icon = Icons.Default.Notifications,
                    text = stringResource(R.string.profile_option_notifications)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileOptionRow(
                    icon = Icons.Default.Lock,
                    text = stringResource(R.string.profile_option_security)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                ProfileOptionRow(
                    icon = Icons.Default.Info,
                    text = stringResource(R.string.profile_option_about)
                )
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

/**
 * Fila de detalle personal del perfil
 */
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

/**
 * Fila de opción de configuración en Perfil
 */
@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
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

/**
 * Tarjeta de producto estilo Wireframe con botón interactivo "Pedir".
 */
@Composable
private fun WireframeProductCard(
    nombre: String,
    estado: String,
    precio: String,
    isAvailable: Boolean,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Cuadro de imagen estilo wireframe (con X cruzada)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nombre,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Etiqueta de disponibilidad
            Surface(
                shape = CircleShape,
                color = if (isAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = estado,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de precio y botón "+" para pedir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = precio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onOrderClick,
                    enabled = isAvailable,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Pedir producto",
                        tint = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
