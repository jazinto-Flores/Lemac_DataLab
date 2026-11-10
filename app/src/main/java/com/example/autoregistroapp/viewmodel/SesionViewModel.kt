package com.example.autoregistroapp.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoregistroapp.data.ApiSimulada
import com.example.autoregistroapp.data.FotoPerfilRepository
import com.example.autoregistroapp.data.Sesion
import com.example.autoregistroapp.data.SesionRepository
import com.example.autoregistroapp.model.TextosLegales
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SesionViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val LIMITE_INACTIVIDAD_MS = 5 * 60 * 1000L   // 5 minutos (2.2)
    }

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

    private var ultimaActividad = System.currentTimeMillis()

    init {
        viewModelScope.launch { _foto.value = fotoRepository.cargar() }

        // Cierre automático por inactividad: mientras haya sesión, revisa cada 10 s
        viewModelScope.launch {
            sesion.map { it?.activa == true }.distinctUntilChanged().collectLatest { activa ->
                if (!activa) return@collectLatest
                ultimaActividad = System.currentTimeMillis()
                while (true) {
                    delay(10_000)
                    if (System.currentTimeMillis() - ultimaActividad >= LIMITE_INACTIVIDAD_MS) {
                        sesionRepository.cerrar()
                        break
                    }
                }
            }
        }
    }

    // ---------- Inactividad / segundo plano ----------
    /** La Activity lo llama en cada toque. */
    fun registrarActividad() { ultimaActividad = System.currentTimeMillis() }

    /** Al volver a la app: si pasaron 5 min o más desde la última actividad, cierra sesión. */
    fun alVolver() {
        if (sesion.value?.activa == true &&
            System.currentTimeMillis() - ultimaActividad >= LIMITE_INACTIVIDAD_MS
        ) {
            viewModelScope.launch { sesionRepository.cerrar() }
        }
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

    /** Valida, pide el token a la API simulada y guarda la sesión (token cifrado). */
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
            val alias = actual.alias.trim().lowercase()
            val token = ApiSimulada.login(alias, actual.clave)
            if (token != null) {
                sesionRepository.iniciar(alias, token)
                _login.value = LoginState()
                ultimaActividad = System.currentTimeMillis()
                onExito()
            } else {
                _login.update { it.copy(cargando = false, errorGeneral = "Usuario o contraseña incorrectos") }
            }
        }
    }

    fun cerrarSesion(onListo: () -> Unit = {}) {
        viewModelScope.launch {
            sesionRepository.cerrar()
            onListo()
        }
    }

    // ---------- Consentimiento informado (2.1) ----------
    fun aceptarConsentimiento(usoEstudios: Boolean) {
        val s = sesion.value ?: return
        viewModelScope.launch {
            runCatching { ApiSimulada.aceptarConsentimiento(s.token, TextosLegales.VERSION, usoEstudios) }
            sesionRepository.guardarConsentimiento(
                s.alias, TextosLegales.VERSION, System.currentTimeMillis(), usoEstudios
            )
        }
    }

    fun cambiarUsoEstudios(valor: Boolean) {
        val s = sesion.value ?: return
        viewModelScope.launch { sesionRepository.cambiarEstudios(s.alias, valor) }
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

    fun avisarFoto(texto: String) { _mensajeFoto.value = texto }
    fun limpiarMensajeFoto() { _mensajeFoto.value = null }
}