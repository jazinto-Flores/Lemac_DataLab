package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autoregistroapp.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegistroViewModel,
    registroId: Int = -1,
    onRegistroGuardado: () -> Unit = {},
    onBack: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("General") }
    var prioridad by remember { mutableStateOf("Media") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (registroId != -1) "Editar Registro" else "Nuevo Registro") },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre / Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        viewModel.guardarRegistro(
                            nombre = nombre,
                            descripcion = descripcion,
                            categoria = categoria,
                            prioridad = prioridad,
                            idActual = if (registroId != -1) registroId else 0
                        )
                        onRegistroGuardado()
                        onBack()
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Registro")
            }
        }
    }
}