package com.cafeuni.app.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generador de imágenes de código QR utilizando la biblioteca ZXing.
 */
object QrCodeGenerator {

    /**
     * Convierte una cadena de texto (ej. código del ticket o menú) en un `ImageBitmap` renderizable en Compose.
     */
    fun generateQrCode(
        content: String,
        size: Int = 512,
        darkColor: Color = Color.Black,
        lightColor: Color = Color.White
    ): ImageBitmap? {
        if (content.isBlank()) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            val darkArgb = darkColor.toArgb()
            val lightArgb = lightColor.toArgb()

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) darkArgb else lightArgb)
                }
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
