package com.example.autoregistroapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.autoregistroapp.data.CatalogoRepository
import com.example.autoregistroapp.data.remote.CatalogoAmbitoDto
import com.example.autoregistroapp.data.remote.MockApiInterceptor
import com.example.autoregistroapp.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Los tres estados de la pantalla: cargando, datos o error. */
data class CatalogoUiState(
    val cargando: Boolean = false,
    val version: String = "",
    val datos: List<CatalogoAmbitoDto> = emptyList(),
    val error: String? = null
)

class CatalogoViewModel : ViewModel() {

    private val repository = CatalogoRepository(RetrofitClient.api)

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    private val _fallaSimulada = MutableStateFlow(MockApiInterceptor.simularFallo)
    val fallaSimulada: StateFlow<Boolean> = _fallaSimulada.asStateFlow()

    fun cargarCatalogos() {
        viewModelScope.launch {
            _uiState.value = CatalogoUiState(cargando = true)
            try {
                val respuesta = repository.obtenerCatalogos()
                _uiState.value = CatalogoUiState(version = respuesta.version, datos = respuesta.catalogos)
            } catch (e: Exception) {
                _uiState.value = CatalogoUiState(
                    error = "No fue posible cargar los catálogos (${e.message ?: "error de red"})"
                )
            }
        }
    }

    /** Solo para la demo: fuerza el estado Error sin necesidad de apagar el Internet del equipo. */
    fun setFallaSimulada(valor: Boolean) {
        _fallaSimulada.value = valor
        MockApiInterceptor.simularFallo = valor
    }
}