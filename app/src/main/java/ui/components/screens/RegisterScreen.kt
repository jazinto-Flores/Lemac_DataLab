package ui.components.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegistroViewModel,
    onRegistroGuardado: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var dropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) {
            snackbarHostState.showSnackbar("Registro guardado exitosamente")
            viewModel.resetSuccessState()
            onRegistroGuardado()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Nuevo Autorregistro") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Selector de Ámbito (DropdownMenu)
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
            ) {
                OutlinedTextField(
                    value = formState.ambito.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ámbito de Registro") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    Ambito.values().forEach { ambito ->
                        DropdownMenuItem(
                            text = { Text(ambito.name) },
                            onClick = {
                                viewModel.onAmbitoChanged(ambito)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. Emoción o Impulso (Validación Obligatoria)
            OutlinedTextField(
                value = formState.emocionOImpulso,
                onValueChange = { viewModel.onEmocionChanged(it) },
                label = { Text("Emoción o Impulso Principal *") },
                isError = formState.emocionError != null,
                supportingText = {
                    formState.emocionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Intensidad (Slider 1 a 5)
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

            // 4. Contexto / Desencadenante (Validación)
            OutlinedTextField(
                value = formState.contexto,
                onValueChange = { viewModel.onContextoChanged(it) },
                label = { Text("Contexto / Desencadenante *") },
                placeholder = { Text("Ej. Trabajo, Casa, Social") },
                isError = formState.contextoError != null,
                supportingText = {
                    formState.contextoError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Notas opcionales (Validación de Longitud)
            OutlinedTextField(
                value = formState.notas,
                onValueChange = { viewModel.onNotasChanged(it) },
                label = { Text("Notas o Estrategia Aplicada") },
                isError = formState.notasError != null,
                supportingText = {
                    formState.notasError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    } ?: Text("${formState.notas.length}/200 caracteres")
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de Guardado
            Button(
                onClick = { viewModel.guardarRegistro() },
                enabled = formState.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar Registro")
            }
        }
    }
}
