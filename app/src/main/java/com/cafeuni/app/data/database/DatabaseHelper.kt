package com.cafeuni.app.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administrador de base de datos SQLite nativo (`DatabaseHelper.kt`).
 *
 * Se encarga de:
 * 1. Crear las 7 tablas oficiales del script SQL en una transacción.
 * 2. Habilitar llaves foráneas (`PRAGMA foreign_keys = ON;`) por conexión.
 * 3. Garantizar la persistencia y gestión de versiones de la base de datos "CafeUni.db".
 */
@Singleton
class DatabaseHelper @Inject constructor(
    @ApplicationContext context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "CafeUni.db"
        const val DATABASE_VERSION = 1
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys = ON;")
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("PRAGMA foreign_keys = ON;")

            // 1. Tabla Usuario
            db.execSQL(
                """
                CREATE TABLE Usuario (
                    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    apellidos TEXT NOT NULL,
                    correo TEXT NOT NULL UNIQUE,
                    contrasena TEXT NOT NULL
                );
                """.trimIndent()
            )

            // 2. Tabla Categoria
            db.execSQL(
                """
                CREATE TABLE Categoria (
                    id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_categoria TEXT NOT NULL,
                    descripcion TEXT
                );
                """.trimIndent()
            )

            // 3. Tabla Producto
            db.execSQL(
                """
                CREATE TABLE Producto (
                    id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    descripcion TEXT,
                    precio REAL NOT NULL,
                    existencia INTEGER NOT NULL DEFAULT 0,
                    disponibilidad INTEGER NOT NULL DEFAULT 1,
                    id_categoria INTEGER NOT NULL,

                    FOREIGN KEY (id_categoria)
                        REFERENCES Categoria(id_categoria),

                    CHECK (precio >= 0),
                    CHECK (existencia >= 0),
                    CHECK (disponibilidad IN (0, 1))
                );
                """.trimIndent()
            )

            // 4. Tabla Pedido
            db.execSQL(
                """
                CREATE TABLE Pedido (
                    id_pedido INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha_pedido TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    hora_pedido TEXT,
                    estado_pedido TEXT NOT NULL,
                    nota_pedido TEXT,
                    foto TEXT,
                    id_usuario INTEGER NOT NULL,

                    FOREIGN KEY (id_usuario)
                        REFERENCES Usuario(id_usuario)
                );
                """.trimIndent()
            )

            // 5. Tabla Detalle_Pedido
            db.execSQL(
                """
                CREATE TABLE Detalle_Pedido (
                    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
                    cantidad INTEGER NOT NULL,
                    subtotal REAL NOT NULL,
                    precio_unitario REAL NOT NULL,
                    id_pedido INTEGER NOT NULL,
                    id_producto INTEGER NOT NULL,

                    FOREIGN KEY (id_pedido)
                        REFERENCES Pedido(id_pedido),

                    FOREIGN KEY (id_producto)
                        REFERENCES Producto(id_producto),

                    CHECK (cantidad > 0),
                    CHECK (subtotal >= 0),
                    CHECK (precio_unitario >= 0)
                );
                """.trimIndent()
            )

            // 6. Tabla Ticket
            db.execSQL(
                """
                CREATE TABLE Ticket (
                    id_ticket INTEGER PRIMARY KEY AUTOINCREMENT,
                    codigo_qr TEXT NOT NULL UNIQUE,
                    fecha_generacion TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    estado_ticket TEXT NOT NULL,
                    id_pedido INTEGER NOT NULL UNIQUE,

                    FOREIGN KEY (id_pedido)
                        REFERENCES Pedido(id_pedido)
                );
                """.trimIndent()
            )

            // 7. Tabla Pago
            db.execSQL(
                """
                CREATE TABLE Pago (
                    id_pago INTEGER PRIMARY KEY AUTOINCREMENT,
                    monto REAL NOT NULL,
                    metodo_pago TEXT NOT NULL DEFAULT 'Efectivo',
                    estado_pago TEXT NOT NULL,
                    id_pedido INTEGER NOT NULL UNIQUE,

                    FOREIGN KEY (id_pedido)
                        REFERENCES Pedido(id_pedido),

                    CHECK (monto >= 0),
                    CHECK (metodo_pago = 'Efectivo')
                );
                """.trimIndent()
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // En futuras versiones se implementarán migraciones sin perder datos existentes
    }
}
