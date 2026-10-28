package com.example.autoregistroapp.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.notifications.ReminderScheduler
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RegistroViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.recordatorio.collectAsStateWithLifecycle()
    var accionPendiente by remember { mutableStateOf<(() -> Unit)?>(null) }
    var avisoPermiso by remember { mutableStateOf<String?>(null) }

    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            avisoPermiso = null
            accionPendiente?.invoke()
        } else {
            avisoPermiso = "Sin permiso de notificaciones no se pueden mostrar recordatorios."
        }
        accionPendiente = null
    }

    // Ejecuta la acción solo si hay permiso; si no, lo solicita primero
    fun conPermiso(accion: () -> Unit) {
        if (ReminderScheduler.tienePermiso(context)) {
            accion()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            accionPendiente = accion
            permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios y privacidad") },
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
            Text("Recordatorio diario", style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Activar recordatorio", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = config.activo,
                    onCheckedChange = { activar ->
                        if (activar) {
                            conPermiso { viewModel.configurarRecordatorio(true, config.hora, config.minuto) }
                        } else {
                            viewModel.configurarRecordatorio(false, config.hora, config.minuto)
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    TimePickerDialog(
                        context,
                        { _, hora, minuto -> viewModel.configurarRecordatorio(config.activo, hora, minuto) },
                        config.hora, config.minuto, true
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hora del recordatorio: %02d:%02d".format(config.hora, config.minuto))
            }

            PrimaryButton(
                text = "Probar notificación (5 s)",
                onClick = { conPermiso { ReminderScheduler.probarEnSegundos(context, 5) } }
            )

            avisoPermiso?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Text(
                "La notificación usa un texto neutro y no se muestra en la pantalla bloqueada, " +
                        "para no revelar contenido sensible.",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            Text("Privacidad y alcance del MVP", style = MaterialTheme.typography.titleLarge)
            Text("• Todos los datos son ficticios o sintéticos.", style = MaterialTheme.typography.bodyMedium)
            Text("• Los registros se guardan solo en este dispositivo.", style = MaterialTheme.typography.bodyMedium)
            Text("• Puedes editar o eliminar cualquiera de tus registros.", style = MaterialTheme.typography.bodyMedium)
            Text("• No se solicitan nombres, RUT, contacto ni datos clínicos.", style = MaterialTheme.typography.bodyMedium)
            Text(
                "• La app no diagnostica, no indica tratamientos ni reemplaza a un profesional. " +
                        "No es un servicio de urgencia.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
