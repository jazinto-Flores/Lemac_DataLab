package com.example.autoregistroapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoregistroapp.data.AppDatabase
import com.example.autoregistroapp.data.PreferenciasRepository
import com.example.autoregistroapp.data.RegistroRepository
import com.example.autoregistroapp.data.ReminderConfig
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    // ViewModel -> Repository -> DAO -> Room
    private val repository = RegistroRepository(AppDatabase.getInstance(appContext).registroDao())
    private val preferencias = PreferenciasRepository(appContext)

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // Todos los registros guardados (vienen de Room)
    val registros: StateFlow<List<Registro>> = repository.registros
        .onEach { _cargando.value = false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Filtro por ámbito (null = todos)
    private val _filtro = MutableStateFlow<Ambito?>(null)
    val filtro: StateFlow<Ambito?> = _filtro.asStateFlow()

    val registrosFiltrados: StateFlow<List<Registro>> =
        combine(registros, _filtro) { lista, filtro ->
            if (filtro == null) lista else lista.filter { it.ambito == filtro }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Estado del formulario
    private val _formState = MutableStateFlow(RegistroFormState())
    val formState: StateFlow<RegistroFormState> = _formState.asStateFlow()

    // Mensaje de confirmación (guardado / actualizado / eliminado)
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    // Recordatorios
    val recordatorio: StateFlow<ReminderConfig> = preferencias.recordatorio
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReminderConfig())

    init {
        viewModelScope.launch {
            if (!preferencias.datosDemoCargados()) {
                repository.sembrarDatosSinteticos()
                preferencias.marcarDatosDemoCargados()
            }
            // Las alarmas se pierden al reiniciar el equipo: se reprograman al abrir la app
            val config = preferencias.recordatorio.first()
            if (config.activo) ReminderScheduler.programar(appContext, config.hora, config.minuto)
        }
    }

    // ---------- Filtro ----------
    fun onFiltroChanged(ambito: Ambito?) {
        _filtro.value = ambito
    }

    // ---------- Formulario ----------
    fun iniciarNuevo() {
        _formState.value = RegistroFormState()
    }

    fun iniciarEdicion(id: String) {
        val r = registros.value.firstOrNull { it.id == id } ?: return iniciarNuevo()
        _formState.value = RegistroFormState(
            editandoId = r.id,
            fechaOriginal = r.fechaHora,
            ambito = r.ambito,
            emocionOImpulso = r.emocionOImpulso,
            intensidad = r.intensidad.toFloat(),
            contexto = r.contexto,
            notas = r.notas
        )
    }

    fun onAmbitoChanged(nuevoAmbito: Ambito) {
        _formState.update {
            // Las opciones dependen del ámbito, por eso se limpia la selección anterior
            it.copy(ambito = nuevoAmbito, emocionOImpulso = "", emocionError = null)
        }
    }

    fun onEmocionChanged(valor: String) {
        _formState.update { it.copy(emocionOImpulso = valor, emocionError = Validador.emocion(valor)) }
    }

    fun onIntensidadChanged(valor: Float) {
        _formState.update { it.copy(intensidad = valor, intensidadError = Validador.intensidad(valor)) }
    }

    fun onContextoChanged(valor: String) {
        _formState.update { it.copy(contexto = valor, contextoError = Validador.contexto(valor)) }
    }

    fun onNotasChanged(valor: String) {
        _formState.update { it.copy(notas = valor, notasError = Validador.notas(valor)) }
    }

    /** Valida y guarda en Room: INSERT si es nuevo, UPDATE si se está editando. */
    fun guardarRegistro() {
        val estado = _formState.value
        if (!estado.esValido()) {
            _formState.value = estado.conErrores()   // muestra todos los errores
            return
        }
        val registro = Registro(
            id = estado.editandoId ?: UUID.randomUUID().toString(),
            ambito = estado.ambito,
            emocionOImpulso = estado.emocionOImpulso,
            intensidad = estado.intensidad.toInt().coerceIn(1, 5),
            contexto = estado.contexto.trim(),
            notas = estado.notas.trim(),
            fechaHora = estado.fechaOriginal ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            if (estado.esEdicion) repository.actualizar(registro) else repository.insertar(registro)
            _mensaje.value = if (estado.esEdicion) "Registro actualizado" else "Registro guardado"
            _formState.value = RegistroFormState(isSuccess = true)
        }
    }

    fun consumirExito() {
        _formState.update { it.copy(isSuccess = false) }
    }

    // ---------- Historial ----------
    fun eliminarRegistro(id: String) {
        viewModelScope.launch {
            val registro = registros.value.firstOrNull { it.id == id } ?: return@launch
            repository.eliminar(registro)
            _mensaje.value = "Registro eliminado"
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    // ---------- Recordatorios (recurso nativo: notificaciones + AlarmManager) ----------
    fun configurarRecordatorio(activo: Boolean, hora: Int, minuto: Int) {
        viewModelScope.launch {
            preferencias.guardarRecordatorio(ReminderConfig(activo, hora, minuto))
            if (activo) ReminderScheduler.programar(appContext, hora, minuto)
            else ReminderScheduler.cancelar(appContext)
        }
    }
}
