package com.example.autoregistroapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoregistroapp.data.CuentasDemo
import com.example.autoregistroapp.data.FotoPerfilRepository
import com.example.autoregistroapp.data.Sesion
import com.example.autoregistroapp.data.SesionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private val sesionRepository = SesionRepository(application.applicationContext)
    private val fotoRepository = FotoPerfilRepository(application.applicationContext)

    // null = todavía se está leyendo la sesión guardada
    val sesion: StateFlow<Sesion?> = sesionRepository.sesion
        .stateIn<Sesion?>(viewModelScope, SharingStarted.Eagerly, null)

    private val _login = MutableStateFlow(LoginState())
    val login: StateFlow<LoginState> = _login.asStateFlow()

    private val _foto = MutableStateFlow<Bitmap?>(null)
    val foto: StateFlow<Bitmap?> = _foto.asStateFlow()

    private val _mensajeFoto = MutableStateFlow<String?>(null)
    val mensajeFoto: StateFlow<String?> = _mensajeFoto.asStateFlow()

    init {
        viewModelScope.launch { _foto.value = fotoRepository.cargar() }
    }

    // ---------- Login ----------
    fun onAliasChanged(valor: String) {
        _login.update { it.copy(alias = valor, aliasError = null, errorGeneral = null) }
    }

    fun onClaveChanged(valor: String) {
        _login.update { it.copy(clave = valor, claveError = null, errorGeneral = null) }
    }

    fun onMostrarClaveChanged(mostrar: Boolean) {
        _login.update { it.copy(mostrarClave = mostrar) }
    }

    /** Valida los campos, comprueba la cuenta ficticia y guarda la sesión. */
    fun iniciarSesion(onExito: () -> Unit) {
        val actual = _login.value
        val aliasError = ValidadorLogin.alias(actual.alias)
        val claveError = ValidadorLogin.clave(actual.clave)
        if (aliasError != null || claveError != null) {
            _login.value = actual.copy(aliasError = aliasError, claveError = claveError)
            return
        }
        viewModelScope.launch {
            _login.update { it.copy(cargando = true, errorGeneral = null) }
            delay(600) // simula la espera de un servicio de autenticación
            if (CuentasDemo.credencialesValidas(actual.alias, actual.clave)) {
                sesionRepository.iniciar(actual.alias.trim().lowercase())
                _login.value = LoginState()
                onExito()
            } else {
                _login.update {
                    it.copy(cargando = false, errorGeneral = "Usuario o contraseña incorrectos")
                }
            }
        }
    }

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            sesionRepository.cerrar()
            onListo()
        }
    }

    // ---------- Foto de perfil ----------
    fun guardarFotoDesdeUri(uri: Uri) {
        viewModelScope.launch {
            val bitmap = fotoRepository.guardarDesdeUri(uri)
            if (bitmap != null) {
                _foto.value = bitmap
                _mensajeFoto.value = "Foto de perfil actualizada"
            } else {
                _mensajeFoto.value = "No se pudo procesar la imagen"
            }
        }
    }

    fun quitarFoto() {
        viewModelScope.launch {
            fotoRepository.eliminar()
            _foto.value = null
            _mensajeFoto.value = "Foto eliminada"
        }
    }

    fun avisarFoto(texto: String) {
        _mensajeFoto.value = texto
    }

    fun limpiarMensajeFoto() {
        _mensajeFoto.value = null
    }
}
