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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.model.fechaIso
import com.example.autoregistroapp.model.fechaLegible
import com.example.autoregistroapp.model.hoyIso
import com.example.autoregistroapp.model.intensidadMaxima
import com.example.autoregistroapp.model.lineas
import com.example.autoregistroapp.ui.components.AvisoCritico
import com.example.autoregistroapp.ui.components.GraficoEvolucion
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.ui.components.RecordCard
import com.example.autoregistroapp.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RegistroViewModel,
    onRegistroHoy: () -> Unit,
    onNavigateToModulo: (Ambito) -> Unit,
    onNavigateToEdit: (Ambito, String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToMisDatos: () -> Unit
) {
    val todos by viewModel.registros.collectAsStateWithLifecycle()
    val registros by viewModel.registrosFiltrados.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val aviso by viewModel.avisoCritico.collectAsStateWithLifecycle()

    var dias by rememberSaveable { mutableIntStateOf(7) }
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

    val hoy = hoyIso()
    val desde = fechaIso(dias - 1)
    val diasConRegistro = todos.map { it.fechaDia }.distinct().count { it >= desde }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi evolución") },
                actions = {
                    IconButton(onClick = onNavigateToMisDatos) {
                        Icon(Icons.Default.Info, contentDescription = "Mis datos")
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Mi perfil")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Notifications, contentDescription = "Recordatorios y privacidad")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Esta app no realiza diagnósticos, no indica tratamientos ni es un servicio de urgencias. " +
                                "Nadie revisa tus registros en tiempo real. Usa solo datos ficticios.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (aviso != null) {
                item { AvisoCritico(onCerrar = viewModel::cerrarAvisoCritico) }
            }

            item {
                AnimatedVisibility(
                    visible = mensaje != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✓ $ultimoMensaje", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            item { PrimaryButton(text = "Registro de hoy", onClick = onRegistroHoy) }

            // Módulos independientes por ámbito
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Módulos", style = MaterialTheme.typography.titleMedium)
                    Ambito.entries.chunked(2).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fila.forEach { a ->
                                val hecho = todos.any { it.ambito == a && it.fechaDia == hoy }
                                OutlinedButton(onClick = { onNavigateToModulo(a) }, modifier = Modifier.weight(1f)) {
                                    Text(a.etiqueta + if (hecho) " ✓" else "")
                                }
                            }
                        }
                    }
                }
            }

            // Mi evolución: un gráfico por ámbito, 7 o 30 días
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Mi evolución", style = MaterialTheme.typography.titleLarge)
                    Text("Días con registro en los últimos $dias: $diasConRegistro", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = dias == 7, onClick = { dias = 7 }, label = { Text("7 días") })
                        FilterChip(selected = dias == 30, onClick = { dias = 30 }, label = { Text("30 días") })
                    }
                }
            }
            items(Ambito.entries) { a ->
                Column {
                    Text(a.etiqueta, style = MaterialTheme.typography.titleSmall)
                    GraficoEvolucion(ambito = a, registros = todos, dias = dias)
                }
            }

            // Historial por fecha con filtro por ámbito
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Historial", style = MaterialTheme.typography.titleLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf<Ambito?>(null) + Ambito.entries) { ambito ->
                            FilterChip(
                                selected = filtro == ambito,
                                onClick = { viewModel.onFiltroChanged(ambito) },
                                label = { Text(ambito?.etiqueta ?: "Todos") }
                            )
                        }
                    }
                }
            }

            when {
                cargando -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                registros.isEmpty() -> item {
                    Text(
                        "Aún no hay registros en este ámbito. Presiona «Registro de hoy» para crear el primero.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> items(registros, key = { it.id }) { item ->
                    RecordCard(
                        ambito = item.ambito.etiqueta,
                        lineas = item.lineas(),
                        notas = item.notas,
                        progreso = item.intensidadMaxima() / 4f,
                        fecha = item.fechaDia.fechaLegible(),
                        editado = item.editado,
                        onEdit = { onNavigateToEdit(item.ambito, item.fechaDia) },
                        onDelete = { registroAEliminar = item },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    // Eliminación lógica con confirmación (4.3)
    registroAEliminar?.let { registro ->
        AlertDialog(
            onDismissRequest = { registroAEliminar = null },
            title = { Text("Eliminar registro") },
            text = {
                Text(
                    "¿Seguro que quieres eliminar el registro de ${registro.ambito.etiqueta} del " +
                            "${registro.fechaDia.fechaLegible()}? Dejará de verse y de contar en tus gráficos."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarRegistro(registro.id)
                    registroAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { registroAEliminar = null }) { Text("Cancelar") } }
        )
    }
}