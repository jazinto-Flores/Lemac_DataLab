package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.data.Sesion
import com.example.autoregistroapp.model.TextosLegales
import com.example.autoregistroapp.model.formatoFecha
import com.example.autoregistroapp.ui.components.FilaSwitch
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.RegistroViewModel
import com.example.autoregistroapp.viewmodel.SesionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisDatosScreen(
    sesionViewModel: SesionViewModel,
    registroViewModel: RegistroViewModel,
    onBack: () -> Unit
) {
    val sesion by sesionViewModel.sesion.collectAsStateWithLifecycle()
    val objetivo by registroViewModel.objetivo.collectAsStateWithLifecycle()
    val sincronizando by registroViewModel.sincronizando.collectAsStateWithLifecycle()
    val mensaje by registroViewModel.mensaje.collectAsStateWithLifecycle()
    var verTexto by remember { mutableStateOf(false) }
    val s = sesion ?: Sesion()

    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(2500)
            registroViewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis datos") },
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
            Text("Usuario: ${s.alias}", style = MaterialTheme.typography.titleMedium)

            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Consentimiento informado", style = MaterialTheme.typography.titleSmall)
                    Text("Versión aceptada: ${s.consentVersion}")
                    if (s.consentFecha > 0) Text("Fecha: ${s.consentFecha.formatoFecha()}")
                    TextButton(onClick = { verTexto = !verTexto }) {
                        Text(if (verTexto) "Ocultar texto" else "Ver texto completo")
                    }
                    if (verTexto) {
                        TextosLegales.noMonitoreada.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                        TextosLegales.secciones.forEach { (t, c) ->
                            Text(t, style = MaterialTheme.typography.labelLarge)
                            Text(c, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            FilaSwitch(
                texto = "Autorizo el uso de mis registros en estudios (opcional)",
                checked = s.usoEstudios,
                onChange = sesionViewModel::cambiarUsoEstudios
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Objetivo personal (adicciones)", style = MaterialTheme.typography.titleSmall)
                    Text(objetivo.ifBlank { "Sin objetivo definido" })
                }
            }

            HorizontalDivider()
            Text("Sincronización", style = MaterialTheme.typography.titleMedium)
            Text(
                if (s.ultimaSync > 0) "Última sincronización: ${s.ultimaSync.formatoFecha()}" else "Aún no sincronizas.",
                style = MaterialTheme.typography.bodyMedium
            )
            PrimaryButton(
                text = if (sincronizando) "Sincronizando..." else "Sincronizar ahora",
                enabled = !sincronizando,
                onClick = { registroViewModel.sincronizar() }
            )
            mensaje?.let { Text("✓ $it", style = MaterialTheme.typography.titleSmall) }

            Text(
                "Tus registros se guardan cifrados en este dispositivo y la sesión se cierra sola " +
                        "tras 5 minutos de inactividad o en segundo plano.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}