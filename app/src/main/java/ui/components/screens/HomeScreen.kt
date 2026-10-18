package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autoregistroapp.ui.components.RecordCard
import com.example.autoregistroapp.viewmodel.RegistroViewModel

@Composable
fun HomeScreen(
    viewModel: RegistroViewModel,
    onNavigateToRegister: () -> Unit
) {
    val registros by viewModel.registros.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mi Diario de Bienestar",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Banner Exclusión Clínica
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️ Este sistema no realiza diagnósticos ni atiende emergencias.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Nuevo Autorregistro")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Historial Reciente", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(registros) { item ->
                RecordCard(
                    ambito = item.ambito.name,
                    detalle = "${item.emocionOImpulso} (${item.contexto}) - ${item.notas}",
                    intensidad = item.intensidad,
                    fecha = item.fechaHora
                )
            }
        }
    }
}