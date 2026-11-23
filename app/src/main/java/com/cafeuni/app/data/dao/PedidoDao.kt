package com.cafeuni.app.data.dao

import android.content.ContentValues
import com.cafeuni.app.data.database.DatabaseHelper
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class TicketPedidoResult(
    val idPedido: Long,
    val idTicket: Long,
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
     * Registra un nuevo pedido y su ticket con código QR único en SQLite mediante una transacción.
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
                put("nota_pedido", "Pedido de $nombreProducto")
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
}
