package com.example.autoregistroapp.model

import java.util.UUID

enum class Ambito {
    ESTADO_ANIMO,
    NEURODESARROLLO,
    DBT,
    ADICCIONES
}

data class Registro(
    val id: String = UUID.randomUUID().toString(),
    val ambito: Ambito,
    val emocionOImpulso: String,
    val intensidad: Int,
    val contexto: String,
    val notas: String = "",
    val fechaHora: String
)