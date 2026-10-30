package com.example.autoregistroapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Un DataStore distinto al de PreferenciasRepository (no pueden compartir nombre)
private val Context.sesionDataStore by preferencesDataStore(name = "sesion")

data class Sesion(
    val activa: Boolean = false,
    val alias: String = ""
)

/** Cuentas ficticias del MVP: no hay servidor ni datos reales. */
object CuentasDemo {
    private val cuentas = mapOf(
        "demo" to "Demo1234",
        "lemac" to "Lemac2026"
    )

    fun credencialesValidas(alias: String, clave: String): Boolean =
        cuentas[alias.trim().lowercase()] == clave
}

class SesionRepository(private val context: Context) {

    private companion object {
        val KEY_ACTIVA = booleanPreferencesKey("sesion_activa")
        val KEY_ALIAS = stringPreferencesKey("sesion_alias")
    }

    val sesion: Flow<Sesion> = context.sesionDataStore.data.map { prefs ->
        Sesion(
            activa = prefs[KEY_ACTIVA] ?: false,
            alias = prefs[KEY_ALIAS] ?: ""
        )
    }

    suspend fun iniciar(alias: String) {
        context.sesionDataStore.edit { prefs ->
            prefs[KEY_ACTIVA] = true
            prefs[KEY_ALIAS] = alias
        }
    }

    suspend fun cerrar() {
        context.sesionDataStore.edit { prefs ->
            prefs[KEY_ACTIVA] = false
            prefs[KEY_ALIAS] = ""
        }
    }
}
