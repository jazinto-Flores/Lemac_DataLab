package com.example.autoregistroapp.data.remote

import androidx.annotation.Keep

/** Respuesta de GET /catalogos. Los nombres de los campos coinciden con las claves del JSON. */
@Keep
data class CatalogosResponse(
    val version: String,
    val catalogos: List<CatalogoAmbitoDto>
)

/** Catálogo de un ámbito: "opciones" (situación, emoción o desencadenante) y "acciones" (habilidad o acción protectora). */
@Keep
data class CatalogoAmbitoDto(
    val ambito: String,
    val opciones: List<String>,
    val acciones: List<String>
)