package com.example.autoregistroapp.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Ambito(val etiqueta: String) {
    ESTADO_ANIMO("Estado de ánimo"),
    NEURODESARROLLO("Neurodesarrollo"),
    DBT("Habilidades DBT"),
    ADICCIONES("Adicciones")
}

enum class EstadoObjetivo(val etiqueta: String) {
    CUMPLIDO("Cumplido"),
    PARCIAL("Parcialmente cumplido"),
    NO_CUMPLIDO("No cumplido")
}

/** Escala común obligatoria 0–4 . */
object Escala {
    val etiquetas = listOf("Sin síntomas", "Leve", "Moderada", "Marcada", "Severa")
    fun etiqueta(n: Int): String = etiquetas.getOrElse(n) { "" }
}

/**
 * Un solo registro por persona, día y ámbito (id determinista).
 * nivel = depresión (ánimo) o intensidad/craving (resto). nivelElevacion solo en ánimo.
 */
data class Registro(
    val usuario: String,
    val fechaDia: String,               // yyyy-MM-dd
    val ambito: Ambito,
    val nivel: Int,                     // 0..4
    val nivelElevacion: Int? = null,    // 0..4, solo ánimo
    val horasSueno: Int? = null,        // solo ánimo
    val riesgoAlto: Boolean = false,    // solo ánimo
    val psicosis: Boolean = false,      // solo ánimo
    val opcion: String = "",
    val accion: String = "",
    val estadoObjetivo: EstadoObjetivo? = null,
    val notas: String = "",
    val modificadoUtc: Long = System.currentTimeMillis(),
    val eliminado: Boolean = false,     // borrado lógico
    val editado: Boolean = false,
    val pendienteSync: Boolean = true
) {
    val id: String get() = idRegistro(usuario, fechaDia, ambito)
}

fun idRegistro(usuario: String, fechaDia: String, ambito: Ambito) = "$usuario|$fechaDia|${ambito.name}"

fun fechaIso(diasAtras: Int = 0): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -diasAtras) }
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}

fun hoyIso(): String = fechaIso(0)

fun String.fechaLegible(): String =
    split("-").let { if (it.size == 3) "${it[2]}/${it[1]}/${it[0]}" else this }

fun Long.formatoFecha(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))

fun Registro.intensidadMaxima(): Int = maxOf(nivel, nivelElevacion ?: 0)

fun Registro.lineas(): List<String> = buildList {
    when (ambito) {
        Ambito.ESTADO_ANIMO -> {
            add("Depresión: $nivel · ${Escala.etiqueta(nivel)}")
            val e = nivelElevacion ?: 0
            add("Elevación: $e · ${Escala.etiqueta(e)}")
            horasSueno?.let { add("Sueño: $it h") }
        }
        Ambito.NEURODESARROLLO -> {
            if (opcion.isNotBlank()) add("Situación: $opcion")
            add("Intensidad: $nivel · ${Escala.etiqueta(nivel)}")
        }
        Ambito.DBT -> {
            if (opcion.isNotBlank()) add("Emoción: $opcion")
            add("Intensidad: $nivel · ${Escala.etiqueta(nivel)}")
            if (accion.isNotBlank()) add("Habilidad: $accion")
        }
        Ambito.ADICCIONES -> {
            add("Craving: $nivel · ${Escala.etiqueta(nivel)}")
            if (opcion.isNotBlank()) add("Desencadenante: $opcion")
            if (accion.isNotBlank()) add("Acción protectora: $accion")
            estadoObjetivo?.let { add("Objetivo: ${it.etiqueta}") }
        }
    }
}

/** Catálogo base cerrado (sintético) + «Otro (especificar)». */
object Catalogo {
    const val OTRO = "Otro (especificar)"
    const val PREFIJO_OTRO = "Otro: "

    /** Neurodesarrollo: situación · DBT: emoción · Adicciones: desencadenante */
    val opciones: Map<Ambito, List<String>> = mapOf(
        Ambito.NEURODESARROLLO to listOf(
            "Sobrecarga sensorial", "Dificultad para organizarme", "Distracción",
            "Rutina cumplida", "Autorregulación lograda", OTRO
        ),
        Ambito.DBT to listOf("Tristeza", "Ansiedad", "Enojo", "Vergüenza", "Soledad", "Alegría", OTRO),
        Ambito.ADICCIONES to listOf("Estrés", "Ambiente social", "Aburrimiento", "Emoción intensa", "Cansancio", OTRO)
    )

    /** DBT: habilidad · Adicciones: acción protectora */
    val acciones: Map<Ambito, List<String>> = mapOf(
        Ambito.DBT to listOf(
            "Mindfulness", "Tolerancia al malestar", "Regulación emocional", "Efectividad interpersonal", OTRO
        ),
        Ambito.ADICCIONES to listOf(
            "Salir a caminar", "Llamar a alguien de confianza", "Respiración consciente", "Actividad distractora", OTRO
        )
    )
}