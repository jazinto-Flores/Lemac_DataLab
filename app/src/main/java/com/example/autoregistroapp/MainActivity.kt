package com.example.autoregistroapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.example.autoregistroapp.navigation.AppNavigation
import com.example.autoregistroapp.notifications.ReminderScheduler
import com.example.autoregistroapp.ui.theme.AutoRegistroAppTheme
import com.example.autoregistroapp.viewmodel.RegistroViewModel
import com.example.autoregistroapp.viewmodel.SesionViewModel

class MainActivity : ComponentActivity() {

    private val registroViewModel: RegistroViewModel by viewModels()
    private val sesionViewModel: SesionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Oculta el contenido en la vista de apps recientes (recomendado por la contraparte).
        // No se usa FLAG_SECURE para no bloquear la grabación de pantalla del video.
        if (Build.VERSION.SDK_INT >= 33) setRecentsScreenshotEnabled(false)
        ReminderScheduler.crearCanal(this)
        setContent {
            AutoRegistroAppTheme {
                Surface {
                    AppNavigation(
                        viewModel = registroViewModel,
                        sesionViewModel = sesionViewModel
                    )
                }
            }
        }
    }

    // Cada toque cuenta como actividad (inactividad máxima: 5 min)
    override fun onUserInteraction() {
        super.onUserInteraction()
        sesionViewModel.registrarActividad()
    }

    // Al volver de segundo plano: si pasaron 5 min o más, se cierra la sesión
    override fun onStart() {
        super.onStart()
        sesionViewModel.alVolver()
    }
}