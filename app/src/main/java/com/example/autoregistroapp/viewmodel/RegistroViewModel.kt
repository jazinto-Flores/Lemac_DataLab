package com.example.autoregistroapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoregistroapp.data.RegistroRepository
import com.example.autoregistroapp.model.RegistroEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RegistroViewModel(private val repository: RegistroRepository) : ViewModel() {

    // Estado reactivo que la interfaz escuchará automáticamente
    val registros: StateFlow<List<RegistroEntity>> = repository.todosLosRegistros
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun guardarRegistro(
        nombre: String,
        descripcion: String,
        categoria: String,
        prioridad: String,
        idActual: Int = 0
    ) {
        if (nombre.isBlank() || nombre.trim().length < 3) return

        viewModelScope.launch {
            val registro = RegistroEntity(
                id = idActual,
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                categoria = categoria,
                prioridad = prioridad
            )
            repository.guardar(registro)
        }
    }

    fun eliminarRegistro(registro: RegistroEntity) {
        viewModelScope.launch {
            repository.eliminar(registro)
        }
    }
}