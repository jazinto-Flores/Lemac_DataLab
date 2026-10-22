package com.example.autoregistroapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.model.formatoFecha
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.ui.components.RecordCard
import com.example.autoregistroapp.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RegistroViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val registros by viewModel.registrosFiltrados.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()

    var registroAEliminar by remember { mutableStateOf<Registro?>(null) }
    var ultimoMensaje by remember { mutableStateOf("") }

    LaunchedEffect(mensaje) {
        val m = mensaje
        if (m != null) {
            ultimoMensaje = m
            delay(2500)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Diario de Bienestar") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Notifications, contentDescription = "Recordatorios y privacidad")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Este MVP no realiza diagnósticos, no indica tratamientos ni atiende emergencias. " +
                            "Usa solo datos ficticios.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }

            AnimatedVisibility(
                visible = mensaje != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "✓ $ultimoMensaje",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            PrimaryButton(text = "+ Nuevo autorregistro", onClick = onNavigateToRegister)
            Spacer(modifier = Modifier.height(12.dp))

            // Filtro por ámbito
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf<Ambito?>(null) + Ambito.entries) { ambito ->
                    FilterChip(
                        selected = filtro == ambito,
                        onClick = { viewModel.onFiltroChanged(ambito) },
                        label = { Text(ambito?.etiqueta ?: "Todos") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val promedio = if (registros.isEmpty()) 0.0 else registros.map { it.intensidad }.average()
            Text(
                text = "Historial: ${registros.size} registros · intensidad promedio ${"%.1f".format(promedio)}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            when {
                cargando -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                registros.isEmpty() -> Text(
                    text = "Aún no hay registros en este ámbito. Presiona “Nuevo autorregistro” para crear el primero.",
                    style = MaterialTheme.typography.bodyMedium
                )
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(registros, key = { it.id }) { item ->
                        RecordCard(
                            ambito = item.ambito.etiqueta,
                            titulo = item.emocionOImpulso,
                            contexto = item.contexto,
                            notas = item.notas,
                            intensidad = item.intensidad,
                            fecha = item.fechaHora.formatoFecha(),
                            onEdit = { onNavigateToEdit(item.id) },
                            onDelete = { registroAEliminar = item },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    registroAEliminar?.let { registro ->
        AlertDialog(
            onDismissRequest = { registroAEliminar = null },
            title = { Text("Eliminar registro") },
            text = { Text("¿Seguro que quieres eliminar este registro de ${registro.ambito.etiqueta}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarRegistro(registro.id)
                    registroAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { registroAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}