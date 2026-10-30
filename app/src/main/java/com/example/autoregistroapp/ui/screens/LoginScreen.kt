package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.viewmodel.SesionViewModel

@Composable
fun LoginScreen(
    viewModel: SesionViewModel,
    onLoginExitoso: () -> Unit
) {
    val estado by viewModel.login.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lemac DataLab", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = estado.alias,
                onValueChange = { viewModel.onAliasChanged(it) },
                label = { Text("Usuario") },
                singleLine = true,
                isError = estado.aliasError != null,
                supportingText = { estado.aliasError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.clave,
                onValueChange = { viewModel.onClaveChanged(it) },
                label = { Text("Contraseña") },
                singleLine = true,
                isError = estado.claveError != null,
                supportingText = { estado.claveError?.let { Text(it) } },
                visualTransformation = if (estado.mostrarClave) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { viewModel.iniciarSesion(onLoginExitoso) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = estado.mostrarClave,
                    onCheckedChange = { viewModel.onMostrarClaveChanged(it) }
                )
                Text("Mostrar contraseña", style = MaterialTheme.typography.bodyMedium)
            }

            estado.errorGeneral?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            PrimaryButton(
                text = if (estado.cargando) "Ingresando..." else "Iniciar sesión",
                onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                enabled = !estado.cargando
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Cuenta de prueba (datos ficticios)", style = MaterialTheme.typography.labelLarge)
                    Text("Usuario: demo", style = MaterialTheme.typography.bodyMedium)
                    Text("Contraseña: Demo1234", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
