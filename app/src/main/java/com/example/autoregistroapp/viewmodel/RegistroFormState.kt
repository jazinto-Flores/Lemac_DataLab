package com.example.autoregistroapp.viewmodel

import com.example.autoregistroapp.model.Ambito
import kotlin.math.roundToInt

data class RegistroFormState(
    val editandoId: String? = null,
    val fechaOriginal: Long? = null,
    val ambito: Ambito = Ambito.ESTADO_ANIMO,
    val emocionOImpulso: String = "",
    val emocionError: String? = null,
    val intensidad: Float = 3f,
    val intensidadError: String? = null,
    val contexto: String = "",
    val contextoError: String? = null,
    val notas: String = "",
    val notasError: String? = null,
    val isSuccess: Boolean = false
) {
    val esEdicion: Boolean get() = editandoId != null
}

/** Reglas de validación del formulario (puras, fáciles de probar con tests unitarios). */
object Validador {
    fun emocion(valor: String): String? =
        if (valor.isBlank()) "Selecciona una opción" else null

    fun contexto(valor: String): String? = when {
        valor.isBlank() -> "El contexto es obligatorio"
        valor.trim().length < 3 -> "Escribe al menos 3 caracteres"
        valor.length > 60 -> "Máximo 60 caracteres"
        else -> null
    }

    fun intensidad(valor: Float): String? =
        if (valor.roundToInt() !in 1..5) "La intensidad debe estar entre 1 y 5" else null

    fun notas(valor: String): String? =
        if (valor.length > 200) "Las notas no pueden superar los 200 caracteres" else null
}

fun RegistroFormState.esValido(): Boolean =
    Validador.emocion(emocionOImpulso) == null &&
            Validador.contexto(contexto) == null &&
            Validador.intensidad(intensidad) == null &&
            Validador.notas(notas) == null

/** Devuelve el estado con TODOS los errores calculados (se usa al presionar Guardar). */
fun RegistroFormState.conErrores(): RegistroFormState = copy(
    emocionError = Validador.emocion(emocionOImpulso),
    contextoError = Validador.contexto(contexto),
    intensidadError = Validador.intensidad(intensidad),
    notasError = Validador.notas(notas)
)
