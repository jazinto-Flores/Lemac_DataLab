package model

import java.util.UUID

data class Registro(
    val id: String = UUID.randomUUID().toString(),
    val ambito: String,
    val detalle: String,
    val intensidad: Int,
    val fecha: String
)