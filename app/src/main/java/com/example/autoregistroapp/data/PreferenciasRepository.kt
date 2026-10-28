package com.example.autoregistroapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lemac_datalab")

data class ReminderConfig(
    val activo: Boolean = false,
    val hora: Int = 20,
    val minuto: Int = 0
)

/** Preferencias simples (recordatorio y bandera de datos de ejemplo). Los registros van en Room. */
class PreferenciasRepository(private val context: Context) {

    private companion object {
        val KEY_DEMO_ROOM = booleanPreferencesKey("datos_demo_room_cargados")
        val KEY_REC_ACTIVO = booleanPreferencesKey("recordatorio_activo")
        val KEY_REC_HORA = intPreferencesKey("recordatorio_hora")
        val KEY_REC_MINUTO = intPreferencesKey("recordatorio_minuto")
    }

    val recordatorio: Flow<ReminderConfig> = context.dataStore.data.map { prefs ->
        ReminderConfig(
            activo = prefs[KEY_REC_ACTIVO] ?: false,
            hora = prefs[KEY_REC_HORA] ?: 20,
            minuto = prefs[KEY_REC_MINUTO] ?: 0
        )
    }

    suspend fun guardarRecordatorio(config: ReminderConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REC_ACTIVO] = config.activo
            prefs[KEY_REC_HORA] = config.hora
            prefs[KEY_REC_MINUTO] = config.minuto
        }
    }

    suspend fun datosDemoCargados(): Boolean =
        context.dataStore.data.first()[KEY_DEMO_ROOM] ?: false

    suspend fun marcarDatosDemoCargados() {
        context.dataStore.edit { prefs -> prefs[KEY_DEMO_ROOM] = true }
    }
}
