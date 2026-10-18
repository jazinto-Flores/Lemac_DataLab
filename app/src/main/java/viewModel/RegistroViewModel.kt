package com.example.autoregistroapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistroViewModel : ViewModel() {

    private val _registros = MutableStateFlow<List<Registro>>(emptyList())
    val registros: StateFlow<List<Registro>> = _registros.asStateFlow()

    private val _formState = MutableStateFlow(RegistroFormState())
    val formState: StateFlow<RegistroFormState> = _formState.asStateFlow()

    init {
        // Datos de prueba iniciales
        _registros.value = listOf(
            Registro(
                ambito = Ambito.ESTADO_ANIMO,
                emocionOImpulso = "Tranquilidad",
                intensidad = 3,
                contexto = "Trabajo",
                notas = "Pausa activa realizada",
                fechaHora = "10:30 AM"
            )
        )
    }

    fun onEmocionChanged(nuevaEmocion: String) {
        _formState.update { currentState ->
            val error = if (nuevaEmocion.isBlank()) "La emoción o impulso es obligatorio" else null
            currentState.copy(
                emocionOImpulso = nuevaEmocion,
                emocionError = error
            )
        }
        validarFormulario()
    }

    fun onContextoChanged(nuevoContexto: String) {
        _formState.update { currentState ->
            val error = if (nuevoContexto.isBlank()) "Debe seleccionar o ingresar un contexto" else null
            currentState.copy(
                contexto = nuevoContexto,
                contextoError = error
            )
        }
        validarFormulario()
    }

    fun onIntensidadChanged(nuevaIntensidad: Float) {
        _formState.update { currentState ->
            val error = if (nuevaIntensidad < 1f || nuevaIntensidad > 5f) "La intensidad debe estar entre 1 y 5" else null
            currentState.copy(
                intensidad = nuevaIntensidad,
                intensidadError = error
            )
        }
        validarFormulario()
    }

    fun onNotasChanged(nuevasNotas: String) {
        _formState.update { currentState ->
            val error = if (nuevasNotas.length > 200) "Las notas no pueden superar los 200 caracteres" else null
            currentState.copy(
                notas = nuevasNotas,
                notasError = error
            )
        }
        validarFormulario()
    }

    fun onAmbitoChanged(nuevoAmbito: Ambito) {
        _formState.update { it.copy(ambito = nuevoAmbito) }
    }

    private fun validarFormulario() {
        val state = _formState.value
        val isValid = state.emocionOImpulso.isNotBlank() &&
                state.contexto.isNotBlank() &&
                state.emocionError == null &&
                state.contextoError == null &&
                state.intensidadError == null &&
                state.notasError == null

        _formState.update { it.copy(isFormValid = isValid) }
    }

    fun guardarRegistro(): Boolean {
        // Validación final
        val state = _formState.value
        if (state.emocionOImpulso.isBlank()) {
            _formState.update { it.copy(emocionError = "La emoción o impulso es obligatorio") }
            return false
        }
        if (state.contexto.isBlank()) {
            _formState.update { it.copy(contextoError = "Debe seleccionar un contexto") }
            return false
        }

        val timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val nuevo = Registro(
            ambito = state.ambito,
            emocionOImpulso = state.emocionOImpulso,
            intensidad = state.intensidad.toInt(),
            contexto = state.contexto,
            notas = state.notas,
            fechaHora = timestamp
        )

        _registros.value = listOf(nuevo) + _registros.value
        _formState.update { RegistroFormState(isSuccess = true) }
        return true
    }

    fun resetSuccessState() {
        _formState.update { it.copy(isSuccess = false) }
    }
}