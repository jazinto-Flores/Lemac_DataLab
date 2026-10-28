package com.example.autoregistroapp

import com.example.autoregistroapp.viewmodel.RegistroFormState
import com.example.autoregistroapp.viewmodel.Validador
import com.example.autoregistroapp.viewmodel.conErrores
import com.example.autoregistroapp.viewmodel.esValido
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroValidacionTest {

    @Test
    fun formularioVacio_noEsValido_yMuestraErrores() {
        val estado = RegistroFormState()
        assertFalse(estado.esValido())
        val conErrores = estado.conErrores()
        assertNotNull(conErrores.emocionError)
        assertNotNull(conErrores.contextoError)
    }

    @Test
    fun formularioCompleto_esValido() {
        val estado = RegistroFormState(emocionOImpulso = "Tranquilidad", contexto = "Casa", intensidad = 3f)
        assertTrue(estado.esValido())
    }

    @Test
    fun contexto_muyCorto_esInvalido() {
        assertNotNull(Validador.contexto("ab"))
        assertNull(Validador.contexto("Casa"))
    }

    @Test
    fun notas_masDe200Caracteres_sonInvalidas() {
        assertNotNull(Validador.notas("a".repeat(201)))
        assertNull(Validador.notas("a".repeat(200)))
    }

    @Test
    fun intensidad_fueraDeRango_esInvalida() {
        assertNotNull(Validador.intensidad(0f))
        assertNotNull(Validador.intensidad(6f))
        assertEquals(null, Validador.intensidad(5f))
    }
}
