package com.cafeuni.app.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilería de seguridad para el encriptado y verificación de contraseñas mediante PBKDF2 con salt único.
 */
@Singleton
class PasswordHasher @Inject constructor() {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256
        private const val SALT_BYTE_SIZE = 16
    }

    /**
     * Genera un hash PBKDF2 con un salt aleatorio único.
     * Retorna el formato "SaltHex:HashHex" para guardarlo en la columna `contrasena`.
     */
    fun hashPassword(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTE_SIZE)
        random.nextBytes(salt)

        val hash = pbkdf2(password.toCharArray(), salt)

        val saltHex = salt.toHexString()
        val hashHex = hash.toHexString()

        return "$saltHex:$hashHex"
    }

    /**
     * Verifica una contraseña ingresada contra el hash almacenado "SaltHex:HashHex".
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false

        val saltHex = parts[0]
        val expectedHashHex = parts[1]

        val salt = saltHex.hexToByteArray()
        val expectedHash = expectedHashHex.hexToByteArray()

        val actualHash = pbkdf2(password.toCharArray(), salt)

        return MessageDigest.isEqual(actualHash, expectedHash)
    }

    private fun pbkdf2(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    private fun String.hexToByteArray(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) + Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
