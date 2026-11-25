package com.example.autoregistroapp

import com.example.autoregistroapp.data.remote.CatalogosResponse
import com.example.autoregistroapp.data.remote.MockApiInterceptor
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Catalogo
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas del JSON de GET /catalogos: conversión a data class y coherencia con el catálogo local. */
class CatalogoApiTest {

    private fun respuesta(): CatalogosResponse =
        Gson().fromJson(MockApiInterceptor.CATALOGOS_JSON, CatalogosResponse::class.java)

    @Test
    fun jsonDelEndpoint_seConvierteADataClass() {
        val r = respuesta()
        assertEquals("2026.11", r.version)
        assertEquals(3, r.catalogos.size)
        assertEquals("NEURODESARROLLO", r.catalogos.first().ambito)
    }

    @Test
    fun catalogoRemoto_coincideConElCatalogoLocal() {
        respuesta().catalogos.forEach { c ->
            val ambito = Ambito.valueOf(c.ambito)
            assertEquals(Catalogo.opciones[ambito], c.opciones)
            assertEquals(Catalogo.acciones[ambito] ?: emptyList<String>(), c.acciones)
        }
    }

    @Test
    fun todasLasListasDeOpcionesPermitenOtro() {
        respuesta().catalogos.forEach { c ->
            assertTrue("Falta 'Otro' en ${c.ambito}", c.opciones.contains(Catalogo.OTRO))
        }
    }
}