package com.example.autoregistroapp.viewmodel

data class LoginState(
    val alias: String = "",
    val clave: String = "",
    val aliasError: String? = null,
    val claveError: String? = null,
    val errorGeneral: String? = null,
    val mostrarClave: Boolean = false,
    val cargando: Boolean = false
)

/** Reglas de validación del login (puras, fáciles de probar con tests unitarios). */
object ValidadorLogin {
    fun alias(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu usuario"
        valor.trim().length < 3 -> "Mínimo 3 caracteres"
        else -> null
    }

    fun clave(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña"
        valor.length < 6 -> "Mínimo 6 caracteres"
        else -> null
    }
}
