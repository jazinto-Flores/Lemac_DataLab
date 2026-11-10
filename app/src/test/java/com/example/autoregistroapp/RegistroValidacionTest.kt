package com.example.autoregistroapp

import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Catalogo
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.viewmodel.RegistroFormState
import com.example.autoregistroapp.viewmodel.Validador
import com.example.autoregistroapp.viewmodel.validar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistroValidacionTest {

    @Test
    fun escala_aceptaSolo0a4() {
        assertNull(Validador.nivel(0))
        assertNull(Validador.nivel(4))
        assertNotNull(Validador.nivel(5))
        assertNotNull(Validador.nivel(-1))
        assertNotNull(Validador.nivel(null))
    }

    @Test
    fun animo_exigeLosDosPolos() {
        val errores = RegistroFormState(ambito = Ambito.ESTADO_ANIMO, nivel = 2).validar()
        assertTrue(errores.containsKey("elevacion"))
        val ok = RegistroFormState(ambito = Ambito.ESTADO_ANIMO, nivel = 2, nivelElevacion = 0).validar()
        assertTrue(ok.isEmpty())
    }

    @Test
    fun otro_exigeEspecificar() {
        assertNotNull(Validador.catalogo(Catalogo.OTRO, "ab", true))
        assertNull(Validador.catalogo(Catalogo.OTRO, "Música", true))
        assertNotNull(Validador.catalogo("", "", true))
        assertNull(Validador.catalogo("", "", false))
    }

    @Test
    fun adicciones_exigeEstadoDelObjetivo() {
        val sin = RegistroFormState(ambito = Ambito.ADICCIONES, nivel = 1).validar()
        assertTrue(sin.containsKey("objetivo"))
        val con = RegistroFormState(
            ambito = Ambito.ADICCIONES, nivel = 1, estadoObjetivo = EstadoObjetivo.CUMPLIDO
        ).validar()
        assertTrue(con.isEmpty())
    }

    @Test
    fun notas_masDe200Caracteres_sonInvalidas() {
        assertNotNull(Validador.notas("a".repeat(201)))
        assertNull(Validador.notas("a".repeat(200)))
        assertEquals(null, Validador.horasSueno(""))
        assertNotNull(Validador.horasSueno("30"))
    }
}