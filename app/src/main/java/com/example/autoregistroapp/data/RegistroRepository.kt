package com.example.autoregistroapp.data

import com.example.autoregistroapp.model.RegistroEntity
import kotlinx.coroutines.flow.Flow

class RegistroRepository(private val registroDao: RegistroDao) {

    // Lista reactiva desde SQLite
    val todosLosRegistros: Flow<List<RegistroEntity>> = registroDao.obtenerTodos()

    suspend fun guardar(registro: RegistroEntity) {
        registroDao.insertarOActualizar(registro)
    }

    suspend fun buscarPorId(id: Int): RegistroEntity? {
        return registroDao.obtenerPorId(id)
    }

    suspend fun eliminar(registro: RegistroEntity) {
        registroDao.eliminar(registro)
    }
}