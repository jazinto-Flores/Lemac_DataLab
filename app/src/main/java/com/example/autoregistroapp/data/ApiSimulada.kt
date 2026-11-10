package com.example.autoregistroapp.data

import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.model.Registro
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

fun Registro.aJson(): JSONObject = JSONObject()
    .put("id", id).put("usuario", usuario).put("fechaDia", fechaDia).put("ambito", ambito.name)
    .put("nivel", nivel)
    .put("nivelElevacion", nivelElevacion ?: JSONObject.NULL)
    .put("horasSueno", horasSueno ?: JSONObject.NULL)
    .put("riesgoAlto", riesgoAlto).put("psicosis", psicosis)
    .put("opcion", opcion).put("accion", accion)
    .put("estadoObjetivo", estadoObjetivo?.name ?: JSONObject.NULL)
    .put("notas", notas)
    .put("modificadoUtc", modificadoUtc)
    .put("eliminado", eliminado).put("editado", editado)

fun JSONObject.aRegistro(): Registro = Registro(
    usuario = getString("usuario"),
    fechaDia = getString("fechaDia"),
    ambito = Ambito.valueOf(getString("ambito")),
    nivel = getInt("nivel"),
    nivelElevacion = if (isNull("nivelElevacion")) null else getInt("nivelElevacion"),
    horasSueno = if (isNull("horasSueno")) null else getInt("horasSueno"),
    riesgoAlto = getBoolean("riesgoAlto"),
    psicosis = getBoolean("psicosis"),
    opcion = getString("opcion"),
    accion = getString("accion"),
    estadoObjetivo = if (isNull("estadoObjetivo")) null else EstadoObjetivo.valueOf(getString("estadoObjetivo")),
    notas = getString("notas"),
    modificadoUtc = getLong("modificadoUtc"),
    eliminado = getBoolean("eliminado"),
    editado = getBoolean("editado"),
    pendienteSync = false
)

/**
 * Backend simulado (interceptor dentro de la app; la contraparte no provee API).
 *   POST /auth/login                      -> token simulado
 *   GET  /consentimiento                  -> consulta
 *   POST /consentimiento                  -> aceptación
 *   PUT  /registros/{id}                  -> crea, edita o elimina (lógico); gana la última modificación
 *   GET  /registros?cambiosDesde={ts}     -> cambios para sincronizar
 */
object ApiSimulada {
    private const val PREFIJO_TOKEN = "token-simulado-"
    private const val LATENCIA_MS = 250L

    private class Fila(val json: JSONObject, val recibidoUtc: Long)

    private val mutex = Mutex()
    private val registros = LinkedHashMap<String, Fila>()
    private val historial = mutableListOf<JSONObject>()      // versiones descartadas (1.1)
    private val consentimientos = mutableMapOf<String, JSONObject>()

    private fun exigirToken(token: String) =
        require(token.startsWith(PREFIJO_TOKEN)) { "401 token inválido" }

    private fun usuarioDe(token: String) = token.removePrefix(PREFIJO_TOKEN)

    suspend fun login(alias: String, clave: String): String? {
        delay(600)
        return if (CuentasDemo.credencialesValidas(alias, clave))
            PREFIJO_TOKEN + alias.trim().lowercase() else null
    }

    suspend fun aceptarConsentimiento(token: String, version: String, estudios: Boolean) =
        mutex.withLock {
            delay(LATENCIA_MS)
            exigirToken(token)
            consentimientos[usuarioDe(token)] = JSONObject()
                .put("version", version)
                .put("aceptadoUtc", System.currentTimeMillis())
                .put("estudios", estudios)
        }

    suspend fun consultarConsentimiento(token: String): JSONObject? = mutex.withLock {
        delay(LATENCIA_MS)
        exigirToken(token)
        consentimientos[usuarioDe(token)]
    }

    /** Devuelve la versión ganadora. La perdedora queda en el historial del servidor. */
    suspend fun guardarRegistro(token: String, json: JSONObject): JSONObject = mutex.withLock {
        delay(LATENCIA_MS)
        exigirToken(token)
        val id = json.getString("id")
        val actual = registros[id]
        val ms = json.getLong("modificadoUtc")
        val ganaNuevo = actual == null || ms > actual.json.getLong("modificadoUtc")
        if (actual != null && ms != actual.json.getLong("modificadoUtc")) {
            historial.add(if (ganaNuevo) actual.json else json)
        }
        if (ganaNuevo) registros[id] = Fila(json, System.currentTimeMillis())
        registros.getValue(id).json
    }

    /** Cambios recibidos desde [desde] + hora del servidor para la próxima consulta. */
    suspend fun cambiosDesde(token: String, desde: Long): Pair<List<JSONObject>, Long> =
        mutex.withLock {
            delay(LATENCIA_MS)
            exigirToken(token)
            val usuario = usuarioDe(token)
            val cambios = registros.values
                .filter { it.recibidoUtc >= desde && it.json.getString("usuario") == usuario }
                .map { it.json }
            cambios to System.currentTimeMillis()
        }
}