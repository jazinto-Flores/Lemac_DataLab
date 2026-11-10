package com.example.autoregistroapp.viewmodel

import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Catalogo
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.model.hoyIso

data class RegistroFormState(
    val ambito: Ambito = Ambito.ESTADO_ANIMO,
    val fecha: String = hoyIso(),
    val esEdicion: Boolean = false,
    val nivel: Int? = null,               // depresión (ánimo) o intensidad/craving
    val nivelElevacion: Int? = null,      // solo ánimo
    val horasSueno: String = "",
    val riesgoAlto: Boolean = false,
    val psicosis: Boolean = false,
    val opcion: String = "",
    val opcionOtro: String = "",
    val accion: String = "",
    val accionOtro: String = "",
    val estadoObjetivo: EstadoObjetivo? = null,
    val notas: String = "",
    val errores: Map<String, String> = emptyMap(),
    val guardado: Boolean = false
)

/** Reglas de validación (puras, fáciles de probar con tests unitarios). */
object Validador {
    fun nivel(valor: Int?): String? =
        if (valor == null || valor !in 0..4) "Selecciona un nivel de 0 a 4" else null

    fun horasSueno(texto: String): String? {
        if (texto.isBlank()) return null
        val h = texto.toIntOrNull()
        return if (h == null || h !in 0..24) "Ingresa horas entre 0 y 24" else null
    }

    fun catalogo(valor: String, otro: String, obligatorio: Boolean): String? = when {
        valor == Catalogo.OTRO && otro.trim().length < 3 -> "Especifica (mínimo 3 caracteres)"
        valor == Catalogo.OTRO && otro.length > 60 -> "Máximo 60 caracteres"
        valor.isBlank() -> if (obligatorio) "Selecciona una opción" else null
        else -> null
    }

    fun notas(valor: String): String? =
        if (valor.length > 200) "Las notas no pueden superar los 200 caracteres" else null
}

fun RegistroFormState.validar(): Map<String, String> = buildMap {
    Validador.nivel(nivel)?.let { put("nivel", it) }
    if (ambito == Ambito.ESTADO_ANIMO) {
        Validador.nivel(nivelElevacion)?.let { put("elevacion", it) }
        Validador.horasSueno(horasSueno)?.let { put("sueno", it) }
    } else {
        Validador.catalogo(opcion, opcionOtro, ambito != Ambito.ADICCIONES)?.let { put("opcion", it) }
    }
    if (ambito == Ambito.DBT || ambito == Ambito.ADICCIONES) {
        Validador.catalogo(accion, accionOtro, false)?.let { put("accion", it) }
    }
    if (ambito == Ambito.ADICCIONES && estadoObjetivo == null) {
        put("objetivo", "Indica el estado de tu objetivo")
    }
    Validador.notas(notas)?.let { put("notas", it) }
}

fun RegistroFormState.aRegistro(usuario: String): Registro {
    fun texto(v: String, otro: String) =
        if (v == Catalogo.OTRO) Catalogo.PREFIJO_OTRO + otro.trim() else v
    val animo = ambito == Ambito.ESTADO_ANIMO
    return Registro(
        usuario = usuario,
        fechaDia = fecha,
        ambito = ambito,
        nivel = nivel ?: 0,
        nivelElevacion = if (animo) nivelElevacion else null,
        horasSueno = if (animo) horasSueno.toIntOrNull() else null,
        riesgoAlto = animo && riesgoAlto,
        psicosis = animo && psicosis,
        opcion = if (animo) "" else texto(opcion, opcionOtro),
        accion = if (ambito == Ambito.DBT || ambito == Ambito.ADICCIONES) texto(accion, accionOtro) else "",
        estadoObjetivo = if (ambito == Ambito.ADICCIONES) estadoObjetivo else null,
        notas = notas.trim(),
        editado = esEdicion
    )
}

fun Registro.aFormulario(): RegistroFormState {
    fun separar(v: String): Pair<String, String> =
        if (v.startsWith(Catalogo.PREFIJO_OTRO)) Catalogo.OTRO to v.removePrefix(Catalogo.PREFIJO_OTRO)
        else v to ""
    val (op, opOtro) = separar(opcion)
    val (ac, acOtro) = separar(accion)
    return RegistroFormState(
        ambito = ambito, fecha = fechaDia, esEdicion = true,
        nivel = nivel, nivelElevacion = nivelElevacion,
        horasSueno = horasSueno?.toString() ?: "",
        riesgoAlto = riesgoAlto, psicosis = psicosis,
        opcion = op, opcionOtro = opOtro, accion = ac, accionOtro = acOtro,
        estadoObjetivo = estadoObjetivo, notas = notas
    )
}