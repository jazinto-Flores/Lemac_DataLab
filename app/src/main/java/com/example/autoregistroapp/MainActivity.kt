package com.example.autoregistroapp

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
}
