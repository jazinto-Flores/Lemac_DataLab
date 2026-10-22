package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Catalogo
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegistroViewModel,
    registroId: String?,
    onRegistroGuardado: () -> Unit,
    onBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var ambitoExpandido by remember { mutableStateOf(false) }
    var emocionExpandida by remember { mutableStateOf(false) }
    var inicializado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(registroId) {
        if (!inicializado) {
            if (registroId == null) viewModel.iniciarNuevo() else viewModel.iniciarEdicion(registroId)
            inicializado = true
        }
    }

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            viewModel.consumirExito()
            onRegistroGuardado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (formState.esEdicion) "Editar registro" else "Nuevo autorregistro") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = ambitoExpandido,
                onExpandedChange = { ambitoExpandido = !ambitoExpandido }
            ) {
                OutlinedTextField(
                    value = formState.ambito.etiqueta,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ámbito de registro") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ambitoExpandido) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = ambitoExpandido,
                    onDismissRequest = { ambitoExpandido = false }
                ) {
                    Ambito.entries.forEach { ambito ->
                        DropdownMenuItem(
                            text = { Text(ambito.etiqueta) },
                            onClick = {
                                viewModel.onAmbitoChanged(ambito)
                                ambitoExpandido = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = emocionExpandida,
                onExpandedChange = { emocionExpandida = !emocionExpandida }
            ) {
                OutlinedTextField(
                    value = formState.emocionOImpulso,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Emoción, situación o impulso *") },
                    isError = formState.emocionError != null,
                    supportingText = { formState.emocionError?.let { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = emocionExpandida) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = emocionExpandida,
                    onDismissRequest = { emocionExpandida = false }
                ) {
                    (Catalogo.opciones[formState.ambito] ?: emptyList()).forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                viewModel.onEmocionChanged(opcion)
                                emocionExpandida = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Intensidad: ${formState.intensidad.toInt()} / 5",
                style = MaterialTheme.typography.bodyLarge
            )
            Slider(
                value = formState.intensidad,
                onValueChange = { viewModel.onIntensidadChanged(it) },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.fillMaxWidth()
            )
            formState.intensidadError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = formState.contexto,
                onValueChange = { viewModel.onContextoChanged(it) },
                label = { Text("Contexto / desencadenante *") },
                placeholder = { Text("Ej. Trabajo, Casa, Social") },
                singleLine = true,
                isError = formState.contextoError != null,
                supportingText = { formState.contextoError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.notas,
                onValueChange = { viewModel.onNotasChanged(it) },
                label = { Text("Notas o estrategia aplicada (opcional)") },
                isError = formState.notasError != null,
                supportingText = {
                    Text(formState.notasError ?: "${formState.notas.length}/200 · No escribas datos personales reales")
                },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            PrimaryButton(
                text = if (formState.esEdicion) "Guardar cambios" else "Guardar registro",
                onClick = { viewModel.guardarRegistro() }
            )
        }
    }
}