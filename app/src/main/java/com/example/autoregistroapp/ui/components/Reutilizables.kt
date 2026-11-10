package com.example.autoregistroapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Tarjeta de registro: muestra «editado» si fue corregido; eliminar pide confirmación en la pantalla. */
@Composable
fun RecordCard(
    ambito: String,
    lineas: List<String>,
    notas: String,
    progreso: Float,
    fecha: String,
    editado: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animado by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(durationMillis = 600),
        label = "intensidad"
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ambito,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = fecha + if (editado) " · editado" else "",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Corregir registro")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar registro")
                }
            }
            lineas.forEach { Text(text = it, style = MaterialTheme.typography.bodyMedium) }
            if (notas.isNotBlank()) {
                Text(text = "Notas: $notas", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = { animado }, modifier = Modifier.fillMaxWidth())
        }
    }
}