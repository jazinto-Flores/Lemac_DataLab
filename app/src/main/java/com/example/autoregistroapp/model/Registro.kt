package com.example.autoregistroapp.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class Ambito(val etiqueta: String) {
    ESTADO_ANIMO("Estado de ánimo"),
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
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))

/** Catálogo sintético de opciones predefinidas por ámbito (solo datos ficticios). */
object Catalogo {
    val opciones: Map<Ambito, List<String>> = mapOf(
        Ambito.ESTADO_ANIMO to listOf(
            "Tranquilidad", "Alegría", "Tristeza", "Ansiedad", "Enojo", "Cansancio"
        ),
        Ambito.NEURODESARROLLO to listOf(
            "Sobrecarga sensorial", "Dificultad para organizarme", "Rutina cumplida",
            "Distracción", "Autorregulación lograda"
        ),
        Ambito.DBT to listOf(
            "Mindfulness", "Tolerancia al malestar", "Regulación emocional",
            "Efectividad interpersonal"
        ),
        Ambito.ADICCIONES to listOf(
            "Impulso / craving", "Desencadenante identificado",
            "Acción protectora realizada", "Objetivo de prueba cumplido"
        )
    )
}
