package com.example.autoregistroapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    // Los eliminados (borrado lógico) no se ven ni cuentan en resúmenes y gráficos
    @Query("SELECT * FROM registros WHERE usuario = :usuario AND eliminado = 0 ORDER BY fechaDia DESC, modificadoUtc DESC")
    fun observarActivos(usuario: String): Flow<List<RegistroEntity>>

    @Query("SELECT * FROM registros WHERE id = :id")
    suspend fun buscar(id: String): RegistroEntity?

    @Query("SELECT * FROM registros WHERE usuario = :usuario AND pendienteSync = 1")
    suspend fun pendientes(usuario: String): List<RegistroEntity>

    @Query("SELECT COUNT(*) FROM registros WHERE usuario = :usuario")
    suspend fun contar(usuario: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(registro: RegistroEntity)

    @Insert
    suspend fun agregarVersion(version: VersionRegistroEntity)

    @Query("SELECT * FROM versiones_registro WHERE registroId = :id ORDER BY modificadoUtc DESC")
    suspend fun versiones(id: String): List<VersionRegistroEntity>
}