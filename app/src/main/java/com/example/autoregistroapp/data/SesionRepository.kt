package com.example.autoregistroapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.autoregistroapp.model.TextosLegales
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sesionDataStore by preferencesDataStore(name = "sesion")

data class Sesion(
    val activa: Boolean = false,
    val alias: String = "",
    val token: String = "",
    val consentVersion: String = "",
    val consentFecha: Long = 0L,
    val usoEstudios: Boolean = false,
    val ultimaSync: Long = 0L
) {
    val consentimientoVigente: Boolean get() = consentVersion == TextosLegales.VERSION
}

data class CuentaDemo(val clave: String, val nombre: String, val objetivo: String)

/** Usuarios predefinidos del dataset sintético. El objetivo viene del perfil (4.2). */
object CuentasDemo {
    private val cuentas = mapOf(
        "demo" to CuentaDemo("Demo1234", "Usuario Demo", "Reducir los días de consumo esta semana"),
        "lemac" to CuentaDemo("Lemac2026", "Usuario Lemac", "Mantener 7 días seguidos sin consumo")
    )

    fun credencialesValidas(alias: String, clave: String): Boolean =
        cuentas[alias.trim().lowercase()]?.clave == clave

    fun objetivoDe(alias: String): String = cuentas[alias]?.objetivo ?: ""
}

class SesionRepository(private val context: Context) {

    private companion object {
        val KEY_ACTIVA = booleanPreferencesKey("sesion_activa")
        val KEY_ALIAS = stringPreferencesKey("sesion_alias")
        val KEY_TOKEN = stringPreferencesKey("sesion_token_cifrado")
    }

    val sesion: Flow<Sesion> = context.sesionDataStore.data.map { prefs ->
        val alias = prefs[KEY_ALIAS] ?: ""
        Sesion(
            activa = prefs[KEY_ACTIVA] ?: false,
            alias = alias,
            token = Cifrador.descifrar(prefs[KEY_TOKEN] ?: ""),   // token cifrado con Keystore
            consentVersion = prefs[stringPreferencesKey("consent_v_$alias")] ?: "",
            consentFecha = prefs[longPreferencesKey("consent_f_$alias")] ?: 0L,
            usoEstudios = prefs[booleanPreferencesKey("estudios_$alias")] ?: false,
            ultimaSync = prefs[longPreferencesKey("sync_$alias")] ?: 0L
        )
    }

    suspend fun iniciar(alias: String, token: String) {
        context.sesionDataStore.edit { prefs ->
            prefs[KEY_ACTIVA] = true
            prefs[KEY_ALIAS] = alias
            prefs[KEY_TOKEN] = Cifrador.cifrar(token)
        }
    }

    /** Cierra sesión y borra el token. */
    suspend fun cerrar() {
        context.sesionDataStore.edit { prefs ->
            prefs[KEY_ACTIVA] = false
            prefs[KEY_ALIAS] = ""
            prefs.remove(KEY_TOKEN)
        }
    }

    suspend fun guardarConsentimiento(alias: String, version: String, fecha: Long, estudios: Boolean) {
        context.sesionDataStore.edit { prefs ->
            prefs[stringPreferencesKey("consent_v_$alias")] = version
            prefs[longPreferencesKey("consent_f_$alias")] = fecha
            prefs[booleanPreferencesKey("estudios_$alias")] = estudios
        }
    }

    suspend fun cambiarEstudios(alias: String, valor: Boolean) {
        context.sesionDataStore.edit { it[booleanPreferencesKey("estudios_$alias")] = valor }
    }

    suspend fun guardarUltimaSync(alias: String, ts: Long) {
        context.sesionDataStore.edit { it[longPreferencesKey("sync_$alias")] = ts }
    }
}