package com.example.autoregistroapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroDao {

    // READ: Room vuelve a emitir la lista cada vez que la tabla cambia
    @Query("SELECT * FROM registros ORDER BY fechaHora DESC")
    fun observarTodos(): Flow<List<RegistroEntity>>

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(registro: RegistroEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(registros: List<RegistroEntity>)

    // UPDATE
    @Update
    suspend fun actualizar(registro: RegistroEntity)

    // DELETE
    @Delete
    suspend fun eliminar(registro: RegistroEntity)
}
