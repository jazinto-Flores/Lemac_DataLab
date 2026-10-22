package com.example.autoregistroapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "lemac_datalab")

data class ReminderConfig(
    val activo: Boolean = false,
    val hora: Int = 20,
    val minuto: Int = 0
)

/**
 * Persistencia local con DataStore (Preferences). Los registros se guardan como JSON.
 * Solo datos ficticios/sintéticos: nada sale del dispositivo.
 */
class RegistroRepository(private val context: Context) {

    private companion object {
        val KEY_REGISTROS = stringPreferencesKey("registros_json")
        val KEY_SEMBRADO = booleanPreferencesKey("datos_demo_cargados")
        val KEY_REC_ACTIVO = booleanPreferencesKey("recordatorio_activo")
        val KEY_REC_HORA = intPreferencesKey("recordatorio_hora")
        val KEY_REC_MINUTO = intPreferencesKey("recordatorio_minuto")
    }

    val registros: Flow<List<Registro>> = context.dataStore.data.map { prefs ->
        decodificar(prefs[KEY_REGISTROS]).sortedByDescending { it.fechaHora }
    }

    val recordatorio: Flow<ReminderConfig> = context.dataStore.data.map { prefs ->
        ReminderConfig(
            activo = prefs[KEY_REC_ACTIVO] ?: false,
            hora = prefs[KEY_REC_HORA] ?: 20,
            minuto = prefs[KEY_REC_MINUTO] ?: 0
        )
    }

    /** Crea o actualiza (si el id ya existe) un registro. */
    suspend fun guardar(registro: Registro) {
        modificar { lista ->
            if (lista.any { it.id == registro.id }) {
                lista.map { if (it.id == registro.id) registro else it }
            } else {
                lista + registro
            }
        }
    }

    suspend fun eliminar(id: String) {
        modificar { lista -> lista.filterNot { it.id == id } }
    }

    suspend fun guardarRecordatorio(config: ReminderConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_REC_ACTIVO] = config.activo
            prefs[KEY_REC_HORA] = config.hora
            prefs[KEY_REC_MINUTO] = config.minuto
        }
    }

    /** Carga una sola vez algunos registros sintéticos para poder probar la app. */
    suspend fun sembrarDatosSinteticosSiCorresponde() {
        context.dataStore.edit { prefs ->
            if (prefs[KEY_SEMBRADO] == true) return@edit
            val ahora = System.currentTimeMillis()
            val hora = 3_600_000L
            val demo = listOf(
                Registro(ambito = Ambito.ESTADO_ANIMO, emocionOImpulso = "Tranquilidad", intensidad = 3,
                    contexto = "Trabajo", notas = "Pausa activa realizada", fechaHora = ahora - 2 * hora),
                Registro(ambito = Ambito.DBT, emocionOImpulso = "Mindfulness", intensidad = 4,
                    contexto = "Casa", notas = "Respiración consciente 5 min", fechaHora = ahora - 26 * hora),
                Registro(ambito = Ambito.NEURODESARROLLO, emocionOImpulso = "Rutina cumplida", intensidad = 2,
                    contexto = "Estudio", notas = "", fechaHora = ahora - 50 * hora),
                Registro(ambito = Ambito.ADICCIONES, emocionOImpulso = "Acción protectora realizada", intensidad = 2,
                    contexto = "Social", notas = "Salí a caminar", fechaHora = ahora - 74 * hora)
            )
            prefs[KEY_REGISTROS] = codificar(demo)
            prefs[KEY_SEMBRADO] = true
        }
    }

    private suspend fun modificar(transformar: (List<Registro>) -> List<Registro>) {
        context.dataStore.edit { prefs: androidx.datastore.preferences.core.MutablePreferences ->
            val actual = decodificar(prefs[KEY_REGISTROS])
            prefs[KEY_REGISTROS] = codificar(transformar(actual))
        }
    }

    private fun codificar(lista: List<Registro>): String {
        val arr = JSONArray()
        lista.forEach { r ->
            arr.put(
                JSONObject()
                    .put("id", r.id)
                    .put("ambito", r.ambito.name)
                    .put("emocion", r.emocionOImpulso)
                    .put("intensidad", r.intensidad)
                    .put("contexto", r.contexto)
                    .put("notas", r.notas)
                    .put("fecha", r.fechaHora)
            )
        }
        return arr.toString()
    }

    private fun decodificar(json: String?): List<Registro> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Registro(
                    id = o.getString("id"),
                    ambito = runCatching { Ambito.valueOf(o.getString("ambito")) }
                        .getOrDefault(Ambito.ESTADO_ANIMO),
                    emocionOImpulso = o.getString("emocion"),
                    intensidad = o.getInt("intensidad"),
                    contexto = o.getString("contexto"),
                    notas = o.optString("notas", ""),
                    fechaHora = o.getLong("fecha")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}