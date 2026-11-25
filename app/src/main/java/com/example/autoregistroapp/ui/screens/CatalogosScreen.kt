package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.viewmodel.CatalogoViewModel

/** T3: GET /catalogos con Retrofit. Muestra los estados Cargando, Datos y Error (con Reintentar). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogosScreen(
    onBack: () -> Unit,
    viewModel: CatalogoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val falla by viewModel.fallaSimulada.collectAsStateWithLifecycle()

    // Al entrar a la pantalla se inicia la carga
    LaunchedEffect(Unit) {
        viewModel.cargarCatalogos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogos (API)") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("GET /catalogos · API simulada con Retrofit", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Simular error de red (demo)", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = falla, onCheckedChange = { viewModel.setFallaSimulada(it) })
            }

            when {
                uiState.cargando -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando catálogos...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                uiState.error != null -> {
                    Text(
                        uiState.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = { viewModel.cargarCatalogos() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Reintentar")
                    }
                }

                else -> {
                    Text("Versión del catálogo: ${uiState.version}", style = MaterialTheme.typography.bodySmall)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.datos) { catalogo ->
                            val nombre = runCatching { Ambito.valueOf(catalogo.ambito) }.getOrNull()?.etiqueta
                                ?: catalogo.ambito
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(nombre, style = MaterialTheme.typography.titleMedium)
                                    Text("Opciones: ${catalogo.opciones.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                                    if (catalogo.acciones.isNotEmpty()) {
                                        Text("Acciones: ${catalogo.acciones.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                    OutlinedButton(onClick = { viewModel.cargarCatalogos() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Recargar")
                    }
                }
            }
        }
    }
}