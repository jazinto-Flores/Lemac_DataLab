package com.example.autoregistroapp.data

import androidx.room.*
import com.example.autoregistroapp.model.RegistroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(registro: RegistroEntity)

    @Query("SELECT * FROM registros ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): RegistroEntity?

    @Delete
    suspend fun eliminar(registro: RegistroEntity)
}