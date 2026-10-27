package com.example.autoregistroapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.autoregistroapp.data.AppDatabase
import com.example.autoregistroapp.data.RegistroRepository
import com.example.autoregistroapp.navigation.AppNavigation
import com.example.autoregistroapp.viewmodel.RegistroViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar la base de datos Room y el DAO
        val database = AppDatabase.getDatabase(this)
        val repository = RegistroRepository(database.registroDao())

        // 2. Crear el ViewModel pasando el repositorio
        val viewModel: RegistroViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return RegistroViewModel(repository) as T
                }
            }
        }

        // 3. Cargar la interfaz de usuario con la navegación
        setContent {
            AppNavigation(viewModel = viewModel)
        }
    }
}