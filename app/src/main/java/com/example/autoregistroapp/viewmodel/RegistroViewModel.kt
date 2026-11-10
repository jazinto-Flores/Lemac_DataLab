package com.example.autoregistroapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoregistroapp.data.AppDatabase
import com.example.autoregistroapp.data.CuentasDemo
import com.example.autoregistroapp.data.PreferenciasRepository
import com.example.autoregistroapp.data.RegistroRepository
import com.example.autoregistroapp.data.ReminderConfig
import com.example.autoregistroapp.data.SesionRepository
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Criticidad
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.model.hoyIso
import com.example.autoregistroapp.model.idRegistro
import com.example.autoregistroapp.notifications.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val repository = RegistroRepository(AppDatabase.getInstance(appContext))
    private val preferencias = PreferenciasRepository(appContext)
    private val sesionRepository = SesionRepository(appContext)

    // Usuario con sesión activa ("" = sin sesión)
    private val usuario: StateFlow<String> = sesionRepository.sesion
        .map { if (it.activa) it.alias else "" }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /** Objetivo personal predefinido en el perfil (4.2). */
    val objetivo: StateFlow<String> = usuario
        .map { CuentasDemo.objetivoDe(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val registros: StateFlow<List<Registro>> = usuario
        .flatMapLatest { u ->
            if (u.isEmpty()) flowOf(emptyList())
            else repository.registros(u).onEach { _cargando.value = false }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _filtro = MutableStateFlow<Ambito?>(null)
    val filtro: StateFlow<Ambito?> = _filtro.asStateFlow()

    val registrosFiltrados: StateFlow<List<Registro>> =
        combine(registros, _filtro) { lista, filtro ->
            if (filtro == null) lista else lista.filter { it.ambito == filtro }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _formState = MutableStateFlow(RegistroFormState())
    val formState: StateFlow<RegistroFormState> = _formState.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Aviso por valor crítico (3.3): se muestra dentro de la app y no bloquea su uso
    private val _avisoCritico = MutableStateFlow<Registro?>(null)
    val avisoCritico: StateFlow<Registro?> = _avisoCritico.asStateFlow()

    private val _sincronizando = MutableStateFlow(false)
    val sincronizando: StateFlow<Boolean> = _sincronizando.asStateFlow()

    val recordatorio: StateFlow<ReminderConfig> = preferencias.recordatorio
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReminderConfig())

    init {
        viewModelScope.launch {
            usuario.collect { u -> if (u.isNotEmpty()) repository.sembrarSiVacio(u) }
        }
        viewModelScope.launch {
            // Las alarmas se pierden al reiniciar el equipo: se reprograman al abrir la app
            val config = preferencias.recordatorio.first()
            if (config.activo) ReminderScheduler.programar(appContext, config.hora, config.minuto)
        }
    }

    fun onFiltroChanged(ambito: Ambito?) { _filtro.value = ambito }

    // ---------- Formulario (un registro por día y ámbito) ----------
    /** Abre el formulario del ámbito; si ya existe registro ese día, lo carga para corregirlo. */
    fun iniciar(ambito: Ambito, fecha: String = hoyIso()) {
        _formState.value = RegistroFormState(ambito = ambito, fecha = fecha)
        viewModelScope.launch {
            val previo = repository.buscar(idRegistro(usuario.value, fecha, ambito))
            if (previo != null) _formState.value = previo.aFormulario()
        }
    }

    private fun editar(clave: String? = null, cambio: (RegistroFormState) -> RegistroFormState) {
        _formState.update { e ->
            val nuevo = cambio(e)
            if (clave == null) nuevo else nuevo.copy(errores = nuevo.errores - clave)
        }
    }

    fun onNivel(v: Int) = editar("nivel") { it.copy(nivel = v) }
    fun onElevacion(v: Int) = editar("elevacion") { it.copy(nivelElevacion = v) }
    fun onHoras(v: String) = editar("sueno") { it.copy(horasSueno = v) }
    fun onRiesgo(v: Boolean) = editar { it.copy(riesgoAlto = v) }
    fun onPsicosis(v: Boolean) = editar { it.copy(psicosis = v) }
    fun onOpcion(v: String) = editar("opcion") { it.copy(opcion = v) }
    fun onOpcionOtro(v: String) = editar("opcion") { it.copy(opcionOtro = v) }
    fun onAccion(v: String) = editar("accion") { it.copy(accion = v) }
    fun onAccionOtro(v: String) = editar("accion") { it.copy(accionOtro = v) }
    fun onObjetivo(v: EstadoObjetivo) = editar("objetivo") { it.copy(estadoObjetivo = v) }
    fun onNotas(v: String) = editar("notas") { it.copy(notas = v) }

    fun guardarRegistro() {
        val estado = _formState.value
        val errores = estado.validar()
        if (errores.isNotEmpty()) {
            _formState.value = estado.copy(errores = errores)
            return
        }
        val registro = estado.aRegistro(usuario.value)
        viewModelScope.launch {
            repository.guardar(registro)
            _mensaje.value = if (estado.esEdicion) "Registro actualizado" else "Registro guardado"
            if (Criticidad.esCritico(registro)) _avisoCritico.value = registro
            _formState.value = estado.copy(guardado = true, errores = emptyMap())
        }
    }

    fun consumirGuardado() { _formState.update { it.copy(guardado = false) } }
    fun cerrarAvisoCritico() { _avisoCritico.value = null }

    // ---------- Historial ----------
    fun eliminarRegistro(id: String) {
        viewModelScope.launch {
            repository.eliminar(id)
            _mensaje.value = "Registro eliminado"
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }

    // ---------- Sincronización con el backend simulado ----------
    fun sincronizar() {
        if (_sincronizando.value) return
        viewModelScope.launch {
            _sincronizando.value = true
            val s = sesionRepository.sesion.first()
            repository.sincronizar(s.alias, s.token, s.ultimaSync)
                .onSuccess {
                    sesionRepository.guardarUltimaSync(s.alias, it)
                    _mensaje.value = "Sincronización completa"
                }
                .onFailure { _mensaje.value = "No se pudo sincronizar" }
            _sincronizando.value = false
        }
    }

    // ---------- Recordatorios (notificaciones locales) ----------
    fun configurarRecordatorio(activo: Boolean, hora: Int, minuto: Int) {
        viewModelScope.launch {
            preferencias.guardarRecordatorio(ReminderConfig(activo, hora, minuto))
            if (activo) ReminderScheduler.programar(appContext, hora, minuto)
            else ReminderScheduler.cancelar(appContext)
        }
    }
}