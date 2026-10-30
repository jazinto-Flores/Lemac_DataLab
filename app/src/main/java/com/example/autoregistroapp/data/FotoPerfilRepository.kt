package com.example.autoregistroapp.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/** Uri temporal donde la app de cámara deja la foto (se comparte con FileProvider). */
fun crearUriFotoTemporal(context: Context): Uri {
    val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
    val archivo = File(carpeta, "captura.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}

/**
 * Guarda la foto de perfil en el almacenamiento privado de la app (solo en este dispositivo).
 * Sirve tanto para fotos tomadas con la cámara como para imágenes elegidas de la galería.
 */
class FotoPerfilRepository(private val context: Context) {

    private companion object {
        const val MAX_LADO = 512
    }

    private fun archivo() = File(context.filesDir, "foto_perfil.jpg")

    suspend fun cargar(): Bitmap? = withContext(Dispatchers.IO) {
        val f = archivo()
        if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
    }

    /** Devuelve el bitmap guardado o null si no se pudo procesar la imagen. */
    suspend fun guardarDesdeUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        val bitmap = decodificar(uri) ?: return@withContext null
        try {
            FileOutputStream(archivo()).use { salida ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, salida)
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    suspend fun eliminar() {
        withContext(Dispatchers.IO) {
            archivo().delete()
        }
    }

    private fun decodificar(uri: Uri): Bitmap? {
        return try {
            val resolver = context.contentResolver

            // 1) Leer solo el tamaño para no cargar una imagen enorme en memoria
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            var muestra = 1
            while (limites.outWidth / muestra > MAX_LADO * 2 || limites.outHeight / muestra > MAX_LADO * 2) {
                muestra *= 2
            }

            // 2) Decodificar reducida
            val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
            val original = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opciones)
            } ?: return null

            // 3) Corregir la rotación según los datos EXIF de la foto
            val rotacion = resolver.openInputStream(uri)?.use { leerRotacion(it) } ?: 0
            val rotado = if (rotacion != 0) {
                Bitmap.createBitmap(
                    original, 0, 0, original.width, original.height,
                    Matrix().apply { postRotate(rotacion.toFloat()) }, true
                )
            } else original

            // 4) Ajustar al tamaño máximo
            val ladoMayor = maxOf(rotado.width, rotado.height)
            if (ladoMayor > MAX_LADO) {
                val factor = MAX_LADO.toFloat() / ladoMayor
                Bitmap.createScaledBitmap(
                    rotado,
                    (rotado.width * factor).toInt().coerceAtLeast(1),
                    (rotado.height * factor).toInt().coerceAtLeast(1),
                    true
                )
            } else rotado
        } catch (e: Exception) {
            null
        }
    }

    private fun leerRotacion(entrada: InputStream): Int =
        when (ExifInterface(entrada).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
}
