package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.model.TextosLegales
import com.example.autoregistroapp.ui.components.FilaCheck
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.SesionViewModel

/** Sin consentimiento aceptado no se puede registrar. Se guarda versión y fecha. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(
    viewModel: SesionViewModel,
    onAceptado: () -> Unit
) {
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    var datos by rememberSaveable { mutableStateOf(false) }
    var salud by rememberSaveable { mutableStateOf(false) }
    var noMonitoreada by rememberSaveable { mutableStateOf(false) }
    var estudios by rememberSaveable { mutableStateOf(false) }

    // Se avanza solo cuando el consentimiento quedó realmente guardado
    LaunchedEffect(sesion?.consentimientoVigente) {
        if (sesion?.consentimientoVigente == true) onAceptado()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Consentimiento informado (v${TextosLegales.VERSION})") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Lee el texto completo. Las tres primeras casillas son obligatorias; la última es voluntaria y puedes cambiarla en «Mis datos».",
                style = MaterialTheme.typography.bodyMedium
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⚠ App de autoayuda no monitoreada", style = MaterialTheme.typography.titleMedium)
                    TextosLegales.noMonitoreada.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                    Text(TextosLegales.EMERGENCIA, style = MaterialTheme.typography.bodyMedium)
                }
            }

            TextosLegales.secciones.forEach { (titulo, cuerpo) ->
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                Text(cuerpo, style = MaterialTheme.typography.bodyMedium)
            }

            HorizontalDivider()
            FilaCheck(TextosLegales.CHECK_DATOS, datos) { datos = it }
            FilaCheck(TextosLegales.CHECK_SALUD, salud) { salud = it }
            FilaCheck(TextosLegales.CHECK_NO_MONITOREADA, noMonitoreada) { noMonitoreada = it }
            FilaCheck(TextosLegales.CHECK_ESTUDIOS, estudios) { estudios = it }

            PrimaryButton(
                text = "Aceptar y continuar",
                enabled = datos && salud && noMonitoreada,
                onClick = { viewModel.aceptarConsentimiento(estudios) }
            )
            TextButton(onClick = { viewModel.cerrarSesion() }, modifier = Modifier.fillMaxWidth()) {
                Text("No acepto: cerrar sesión")
            }
        }
    }
}