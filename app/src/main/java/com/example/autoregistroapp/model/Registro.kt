package com.example.autoregistroapp.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


enum class Ambito(val etiqueta: String){
    ESTADO_ANIMO("Estado de animo"),
    NEURODESARROLLO("Neurodesarrollo"),
    DBT("Habilidades DBT"),
    ADICCIONES("Adicciones")
}

data class Registro(
    val id: String = UUID.randomUUID().toString(),
    val ambito: Ambito,
    val emocionOImpulso: String,
    val intensidad: Int,
    val contexto: String,
    val notas: String = "",
    val fechaHora: Long = System.currentTimeMillis()
)

fun Long.formatoFecha(): String =
    SimpleDateFormat("dd/MM/yyy HH:mm", Locale.getDefault()).format(Date(this))

object Catalogo {
    val opciones: Map<Ambito, List<String>> = mapOf(
        Ambito.ESTADO_ANIMO to listOf(
            "Tranquilidad", "Alegria", "Tristeza", "Enojo", "Cansancio"
        ),
        Ambito.NEURODESARROLLO to listOf(
            "Sobrecarga sensorial", "Dificultad para organizarme", "Rutina cumplida",
            "Distraccion", "Autorregulacion lograda"
        ),
        Ambito.DBT to listOf(
            "Mindfulness", "Tolerancia al malestar", "Regulacion emocional",
            "Efectividad interpersonal"
        ),
        Ambito.ADICCIONES to listOf(
            "Impulso / craving", "Desencadenante identificado",
            "Accion protectora realizada", "Objetivo de prueba cumplido"
        )
    )
}