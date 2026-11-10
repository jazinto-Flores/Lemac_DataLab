package com.example.autoregistroapp

import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Conflictos
import com.example.autoregistroapp.model.Criticidad
import com.example.autoregistroapp.model.Registro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasNegocioTest {

    private fun reg(a: Ambito, nivel: Int, ms: Long = 1L, sueno: Int? = null,
                    riesgo: Boolean = false, psicosis: Boolean = false) =
        Registro("demo", "2026-11-10", a, nivel, if (a == Ambito.ESTADO_ANIMO) 0 else null,
            sueno, riesgo, psicosis, modificadoUtc = ms)

    @Test
    fun craving4_yEmocion4_sonCriticos() {
        assertTrue(Criticidad.esCritico(reg(Ambito.ADICCIONES, 4)))
        assertTrue(Criticidad.esCritico(reg(Ambito.DBT, 4)))
        assertFalse(Criticidad.esCritico(reg(Ambito.ADICCIONES, 3)))
    }

    @Test
    fun animo_critico_porRiesgo_psicosisOSuenoMenorA3() {
        assertTrue(Criticidad.esCritico(reg(Ambito.ESTADO_ANIMO, 1, riesgo = true)))
        assertTrue(Criticidad.esCritico(reg(Ambito.ESTADO_ANIMO, 1, psicosis = true)))
        assertTrue(Criticidad.esCritico(reg(Ambito.ESTADO_ANIMO, 1, sueno = 2)))
        assertFalse(Criticidad.esCritico(reg(Ambito.ESTADO_ANIMO, 1, sueno = 3)))
    }

    @Test
    fun registroEliminado_noEsCritico() {
        assertFalse(Criticidad.esCritico(reg(Ambito.DBT, 4).copy(eliminado = true)))
    }

    @Test
    fun ultimaModificacionGana() {
        val local = reg(Ambito.DBT, 1, ms = 100)
        val remotoNuevo = reg(Ambito.DBT, 3, ms = 200)
        assertEquals(remotoNuevo, Conflictos.ganador(local, remotoNuevo))
        val remotoViejo = reg(Ambito.DBT, 3, ms = 50)
        assertEquals(local, Conflictos.ganador(local, remotoViejo))
        assertEquals(local, Conflictos.ganador(local, local.copy(nivel = 2)))   // empate: local
    }
}