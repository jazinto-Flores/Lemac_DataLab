package com.example.autoregistroapp.data

import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository: único punto de acceso a los registros.
 * El ViewModel no conoce Room; solo habla con esta clase, y esta con el DAO.
 */
class RegistroRepository(private val dao: RegistroDao) {

    val registros: Flow<List<Registro>> =
        dao.observarTodos().map { lista -> lista.map { it.aModelo() } }

    suspend fun insertar(registro: Registro) = dao.insertar(registro.aEntidad())

    suspend fun actualizar(registro: Registro) = dao.actualizar(registro.aEntidad())

    suspend fun eliminar(registro: Registro) = dao.eliminar(registro.aEntidad())

    /** Registros ficticios para poder probar la app (se cargan una sola vez). */
    suspend fun sembrarDatosSinteticos() {
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
        dao.insertarVarios(demo.map { it.aEntidad() })
    }
}
