package com.example.autoregistroapp.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** AES-256-GCM con clave en Android Keystore (la clave nunca sale del sistema). */
object Cifrador {
    private const val ALIAS = "lemac_datalab_key"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12

    private fun clave(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(
                ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    fun cifrar(texto: String): String {
        if (texto.isEmpty()) return ""
        val c = Cipher.getInstance(TRANSFORMACION)
        c.init(Cipher.ENCRYPT_MODE, clave())
        val cifrado = c.doFinal(texto.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(c.iv + cifrado, Base64.NO_WRAP)
    }

    fun descifrar(dato: String): String {
        if (dato.isEmpty()) return ""
        return try {
            val raw = Base64.decode(dato, Base64.NO_WRAP)
            val c = Cipher.getInstance(TRANSFORMACION)
            c.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(128, raw, 0, IV_BYTES))
            String(c.doFinal(raw, IV_BYTES, raw.size - IV_BYTES), Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}