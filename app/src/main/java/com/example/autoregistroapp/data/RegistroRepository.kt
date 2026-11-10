package com.example.autoregistroapp.data

import androidx.room.withTransaction
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Conflictos
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.model.fechaIso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RegistroRepository(private val db: AppDatabase) {

    private val dao = db.registroDao()

    fun registros(usuario: String): Flow<List<Registro>> =
        dao.observarActivos(usuario).map { lista -> lista.map { it.aModelo() } }

    suspend fun buscar(id: String): Registro? =
        dao.buscar(id)?.takeIf { !it.eliminado }?.aModelo()

    private fun versionDe(r: Registro, motivo: String) = VersionRegistroEntity(
        registroId = r.id,
        modificadoUtc = r.modificadoUtc,
        motivo = motivo,
        contenido = Cifrador.cifrar(r.aJson().toString())
    )

    /** Crea o corrige. Si ya existía (mismo día y ámbito) la versión anterior queda en el historial. */
    suspend fun guardar(nuevo: Registro) = db.withTransaction {
        val previo = dao.buscar(nuevo.id)?.aModelo()
        if (previo != null) {
            dao.agregarVersion(versionDe(previo, if (previo.eliminado) "registro eliminado" else "edición"))
        }
        dao.guardar(
            nuevo.copy(
                editado = previo != null,
                eliminado = false,
                modificadoUtc = System.currentTimeMillis(),
                pendienteSync = true
            ).aEntidad()
        )
    }

    /** Eliminación lógica: deja de verse y de contar, y se sincroniza como eliminado. */
    suspend fun eliminar(id: String) = db.withTransaction {
        val previo = dao.buscar(id)?.aModelo() ?: return@withTransaction
        dao.agregarVersion(versionDe(previo, "eliminación"))
        dao.guardar(
            previo.copy(
                eliminado = true,
                modificadoUtc = System.currentTimeMillis(),
                pendienteSync = true
            ).aEntidad()
        )
    }

    suspend fun versiones(id: String) = dao.versiones(id)

    // ---------- Sincronización (regla 1.1: última modificación gana, por registro) ----------

    private suspend fun aplicar(remoto: Registro, local: Registro?) = db.withTransaction {
        if (local == null) {
            dao.guardar(remoto.copy(pendienteSync = false).aEntidad())
        } else {
            val gana = Conflictos.ganador(local, remoto)
            if (gana.modificadoUtc != local.modificadoUtc) {
                dao.agregarVersion(versionDe(local, "reemplazada al sincronizar (última modificación gana)"))
                dao.guardar(remoto.copy(pendienteSync = false).aEntidad())
            } else if (local.pendienteSync && gana.modificadoUtc == remoto.modificadoUtc) {
                dao.guardar(local.copy(pendienteSync = false).aEntidad())
            }
        }
    }

    /** Devuelve la hora del servidor para guardarla como "última sincronización". */
    suspend fun sincronizar(usuario: String, token: String, desde: Long): Result<Long> = runCatching {
        dao.pendientes(usuario).forEach { pendiente ->
            val local = pendiente.aModelo()
            val ganador = ApiSimulada.guardarRegistro(token, local.aJson()).aRegistro()
            aplicar(ganador, local)
        }
        val (cambios, ahora) = ApiSimulada.cambiosDesde(token, desde)
        cambios.forEach { json ->
            val remoto = json.aRegistro()
            aplicar(remoto, dao.buscar(remoto.id)?.aModelo())
        }
        ahora
    }

    /** Datos ficticios (últimos 10 días) para poder ver los gráficos. Hoy queda libre. */
    suspend fun sembrarSiVacio(usuario: String) {
        if (dao.contar(usuario) > 0) return
        val dia = 86_400_000L
        val ahora = System.currentTimeMillis()
        fun r(d: Int, a: Ambito, nivel: Int, elev: Int? = null, sueno: Int? = null,
              op: String = "", ac: String = "", obj: EstadoObjetivo? = null) =
            Registro(
                usuario, fechaIso(d), a, nivel, elev, sueno,
                opcion = op, accion = ac, estadoObjetivo = obj,
                notas = "Dato de ejemplo", modificadoUtc = ahora - d * dia
            )

        val animo = listOf(1 to 0, 1 to 0, 2 to 0, 2 to 1, 1 to 1, 0 to 1, 0 to 0, 1 to 0, 2 to 0, 1 to 0)
        val demo = animo.mapIndexed { i, (dep, el) ->
            r(i + 1, Ambito.ESTADO_ANIMO, dep, el, sueno = 6 + i % 3)
        } + listOf(
            r(1, Ambito.NEURODESARROLLO, 2, op = "Distracción"),
            r(3, Ambito.NEURODESARROLLO, 1, op = "Rutina cumplida"),
            r(5, Ambito.NEURODESARROLLO, 3, op = "Sobrecarga sensorial"),
            r(2, Ambito.DBT, 2, op = "Ansiedad", ac = "Mindfulness"),
            r(4, Ambito.DBT, 3, op = "Enojo", ac = "Tolerancia al malestar"),
            r(6, Ambito.DBT, 1, op = "Tristeza", ac = "Regulación emocional"),
            r(1, Ambito.ADICCIONES, 1, op = "Estrés", ac = "Salir a caminar", obj = EstadoObjetivo.CUMPLIDO),
            r(2, Ambito.ADICCIONES, 2, op = "Aburrimiento", ac = "Respiración consciente", obj = EstadoObjetivo.PARCIAL),
            r(4, Ambito.ADICCIONES, 3, op = "Ambiente social", obj = EstadoObjetivo.NO_CUMPLIDO),
            r(7, Ambito.ADICCIONES, 1, op = "Cansancio", ac = "Salir a caminar", obj = EstadoObjetivo.CUMPLIDO)
        )
        demo.forEach { dao.guardar(it.aEntidad()) }
    }
}