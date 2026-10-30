package com.example.autoregistroapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.data.crearUriFotoTemporal
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.SesionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SesionViewModel,
    onBack: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val foto by viewModel.foto.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensajeFoto.collectAsStateWithLifecycle()
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }

    // Cámara: la app de cámara del sistema guarda la foto en el Uri que le pasamos
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val uri = uriCamara
        if (exito && uri != null) viewModel.guardarFotoDesdeUri(uri)
    }

    // Galería: selector de fotos de Android (no requiere permisos)
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.guardarFotoDesdeUri(uri)
    }

    // El aviso desaparece solo a los 3 segundos
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            delay(3000)
            viewModel.limpiarMensajeFoto()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto de perfil en círculo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = foto
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Sin foto de perfil",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Text(
                text = sesion?.alias.orEmpty().ifBlank { "Usuario" },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Cuenta ficticia de prueba",
                style = MaterialTheme.typography.bodySmall
            )

            mensaje?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            }

            PrimaryButton(
                text = "Tomar foto",
                onClick = {
                    try {
                        val uri = crearUriFotoTemporal(context)
                        uriCamara = uri
                        camaraLauncher.launch(uri)
                    } catch (e: Exception) {
                        viewModel.avisarFoto("No se pudo abrir la cámara en este dispositivo")
                    }
                }
            )

            OutlinedButton(
                onClick = {
                    galeriaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Elegir de la galería")
            }

            if (foto != null) {
                TextButton(onClick = { viewModel.quitarFoto() }) {
                    Text("Quitar foto")
                }
            }

            Text(
                text = "La foto se guarda solo en este dispositivo.",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            OutlinedButton(
                onClick = { viewModel.cerrarSesion(onCerrarSesion) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
