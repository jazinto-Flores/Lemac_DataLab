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
import com.example.autoregistroapp.ui.components.AvisoCritico
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.RegistroViewModel

/** «Registro de hoy»: recorre los 4 ámbitos, se puede omitir cualquiera; cada uno se guarda por separado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroHoyScreen(
    viewModel: RegistroViewModel,
    onTerminar: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val objetivo by viewModel.objetivo.collectAsStateWithLifecycle()
    val aviso by viewModel.avisoCritico.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    val ambitos = Ambito.entries
    var paso by rememberSaveable { mutableIntStateOf(0) }
    val ambito = ambitos[paso]

    fun avanzar() {
        if (paso < ambitos.lastIndex) paso++ else onTerminar()
    }

    LaunchedEffect(paso) {
        viewModel.iniciar(ambito)
        scroll.scrollTo(0)
    }
    LaunchedEffect(form.guardado) {
        if (form.guardado) {
            viewModel.consumirGuardado()
            avanzar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de hoy") },
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
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LinearProgressIndicator(progress = { (paso + 1) / ambitos.size.toFloat() }, modifier = Modifier.fillMaxWidth())
            Text(
                "Paso ${paso + 1} de ${ambitos.size} · ${ambito.etiqueta}",
                style = MaterialTheme.typography.titleLarge
            )
            if (aviso != null) AvisoCritico(onCerrar = viewModel::cerrarAvisoCritico)

            FormularioAmbito(viewModel, form, objetivo)

            PrimaryButton(
                text = if (paso == ambitos.lastIndex) "Guardar y terminar" else "Guardar y continuar",
                onClick = { viewModel.guardarRegistro() }
            )
            OutlinedButton(onClick = { avanzar() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (paso == ambitos.lastIndex) "Omitir y terminar" else "Omitir este ámbito")
            }
        }
    }
}