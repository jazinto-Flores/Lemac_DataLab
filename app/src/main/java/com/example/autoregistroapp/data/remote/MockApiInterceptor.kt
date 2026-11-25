package com.example.autoregistroapp.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * API simulada: intercepta la solicitud de Retrofit y responde con el JSON de prueba, sin salir a Internet.
 * Así el flujo Retrofit es real (GET -> JSON -> data class) y la app funciona sin conexión.
 */
class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        Thread.sleep(LATENCIA_MS) // latencia simulada: permite ver el estado "Cargando"
        if (simularFallo) throw IOException("Sin conexión (simulada)")

        val esCatalogos = request.method == "GET" && request.url.encodedPath == "/catalogos"
        val codigo = if (esCatalogos) 200 else 404
        val cuerpo = if (esCatalogos) CATALOGOS_JSON else """{"error":"Ruta no encontrada"}"""

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(codigo)
            .message(if (esCatalogos) "OK" else "Not Found")
            .body(cuerpo.toResponseBody("application/json".toMediaType()))
            .build()
    }

    companion object {
        private const val LATENCIA_MS = 800L

        /** Interruptor para la demo del estado Error. */
        @Volatile
        var simularFallo: Boolean = false

        /** JSON de ejemplo de GET /catalogos (mismos textos que el catálogo local `Catalogo`). */
        const val CATALOGOS_JSON = """{"version":"2026.11","catalogos":[{"ambito":"NEURODESARROLLO","opciones":["Sobrecarga sensorial","Dificultad para organizarme","Distracción","Rutina cumplida","Autorregulación lograda","Otro (especificar)"],"acciones":[]},{"ambito":"DBT","opciones":["Tristeza","Ansiedad","Enojo","Vergüenza","Soledad","Alegría","Otro (especificar)"],"acciones":["Mindfulness","Tolerancia al malestar","Regulación emocional","Efectividad interpersonal","Otro (especificar)"]},{"ambito":"ADICCIONES","opciones":["Estrés","Ambiente social","Aburrimiento","Emoción intensa","Cansancio","Otro (especificar)"],"acciones":["Salir a caminar","Llamar a alguien de confianza","Respiración consciente","Actividad distractora","Otro (especificar)"]}]}"""
    }
}