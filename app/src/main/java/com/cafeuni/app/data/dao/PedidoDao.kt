package com.cafeuni.app.data.dao

import android.content.ContentValues
import com.cafeuni.app.data.database.DatabaseHelper
import com.cafeuni.app.models.CartItem
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class TicketPedidoResult(
    val idPedido: Long,
    val idTicket: Long,
    val codigoQr: String
)

data class PedidoRegistroModel(
    val idPedido: Long,
    val fechaPedido: String,
    val estadoPedido: String,
    val notaPedido: String,
    val codigoQr: String
)

/**
 * Data Access Object (DAO) para las operaciones reales de Pedido, Detalle_Pedido y Ticket en SQLite.
 */
@Singleton
class PedidoDao @Inject constructor(
    private val dbHelper: DatabaseHelper
) {

    /**
     * Registra un pedido individual y su ticket con código QR en SQLite.
     */
    fun crearPedidoConTicket(
        idUsuario: Long,
        nombreProducto: String,
        precio: Double
    ): TicketPedidoResult {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // 1. Insertar en Pedido
            val valuesPedido = ContentValues().apply {
                put("estado_pedido", "Pendiente")
                put("nota_pedido", "1x $nombreProducto ($${"%.2f".format(precio)})")
                put("id_usuario", if (idUsuario > 0) idUsuario else 1)
            }
            val idPedido = db.insert("Pedido", null, valuesPedido)

            // 2. Insertar en Ticket con código QR único
            val codigoQr = "TICKET-CAFEEUNI-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
            val valuesTicket = ContentValues().apply {
                put("codigo_qr", codigoQr)
                put("estado_ticket", "Activo")
                put("id_pedido", idPedido)
            }
            val idTicket = db.insert("Ticket", null, valuesTicket)

            db.setTransactionSuccessful()
            return TicketPedidoResult(idPedido = idPedido, idTicket = idTicket, codigoQr = codigoQr)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Registra un pedido múltiple desde el Carrito de Compras en SQLite mediante transacción.
     */
    fun crearPedidoMultipleConTicket(
        idUsuario: Long,
        items: List<CartItem>,
        total: Double
    ): TicketPedidoResult {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val resumen = items.joinToString(", ") { "${it.cantidad}x ${it.producto.nombre}" }

            // 1. Insertar en Pedido
            val valuesPedido = ContentValues().apply {
                put("estado_pedido", "Pendiente")
                put("nota_pedido", resumen)
                put("id_usuario", if (idUsuario > 0) idUsuario else 1)
            }
            val idPedido = db.insert("Pedido", null, valuesPedido)

            // 2. Insertar en Detalle_Pedido para cada producto
            items.forEach { item ->
                val valuesDetalle = ContentValues().apply {
                    put("cantidad", item.cantidad)
                    put("subtotal", item.subtotal)
                    put("precio_unitario", item.producto.precio)
                    put("id_pedido", idPedido)
                    put("id_producto", item.producto.id)
                }
                db.insert("Detalle_Pedido", null, valuesDetalle)
            }

            // 3. Insertar en Ticket con código QR único
            val codigoQr = "TICKET-CAFEEUNI-" + UUID.randomUUID().toString().substring(0, 8).uppercase()
            val valuesTicket = ContentValues().apply {
                put("codigo_qr", codigoQr)
                put("estado_ticket", "Activo")
                put("id_pedido", idPedido)
            }
            val idTicket = db.insert("Ticket", null, valuesTicket)

            db.setTransactionSuccessful()
            return TicketPedidoResult(idPedido = idPedido, idTicket = idTicket, codigoQr = codigoQr)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Obtiene el historial real de pedidos y tickets de un usuario desde SQLite.
     */
    fun obtenerPedidosPorUsuario(idUsuario: Long): List<PedidoRegistroModel> {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT p.id_pedido, p.fecha_pedido, p.estado_pedido, p.nota_pedido, t.codigo_qr
            FROM Pedido p
            INNER JOIN Ticket t ON p.id_pedido = t.id_pedido
            WHERE p.id_usuario = ?
            ORDER BY p.id_pedido DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(if (idUsuario > 0) idUsuario.toString() else "1"))
        val lista = mutableListOf<PedidoRegistroModel>()

        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    PedidoRegistroModel(
                        idPedido = it.getLong(it.getColumnIndexOrThrow("id_pedido")),
                        fechaPedido = it.getString(it.getColumnIndexOrThrow("fecha_pedido")),
                        estadoPedido = it.getString(it.getColumnIndexOrThrow("estado_pedido")),
                        notaPedido = it.getString(it.getColumnIndexOrThrow("nota_pedido")),
                        codigoQr = it.getString(it.getColumnIndexOrThrow("codigo_qr"))
                    )
                )
            }
        }
        return lista
    }
}
