package com.example.autoregistroapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro

/** Tabla "registros" en SQLite. El ámbito se guarda como texto (nombre del enum). */
@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey val id: String,
    val ambito: String,
    val emocionOImpulso: String,
    val intensidad: Int,
    val contexto: String,
    val notas: String,
    val fechaHora: Long
)

fun RegistroEntity.aModelo(): Registro = Registro(
    id = id,
    ambito = runCatching { Ambito.valueOf(ambito) }.getOrDefault(Ambito.ESTADO_ANIMO),
    emocionOImpulso = emocionOImpulso,
    intensidad = intensidad,
    contexto = contexto,
    notas = notas,
    fechaHora = fechaHora
)

fun Registro.aEntidad(): RegistroEntity = RegistroEntity(
    id = id,
    ambito = ambito.name,
    emocionOImpulso = emocionOImpulso,
    intensidad = intensidad,
    contexto = contexto,
    notas = notas,
    fechaHora = fechaHora
)
