package ui.components.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ejemplo.autodiario.ui.components.PrimaryButton
import com.ejemplo.autodiario.ui.components.RecordCard
import com.ejemplo.autodiario.viewmodel.RegistroViewModel

@Composable
fun HomeScreen(viewModel: RegistroViewModel) {
    val listaRegistros by viewModel.registros.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mi Diario de Bienestar",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card de Exclusión Clínica / Emergencias
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "⚠️ Este sistema no realiza diagnósticos ni atiende emergencias.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de acción para simular nuevo registro
        PrimaryButton(
            text = "Simular Nuevo Registro (+ Ánimo)",
            onClick = {
                viewModel.agregarRegistro("Estado de Ánimo", "Registro rápido realizado", 4)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Historial Reciente",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Listado de registros utilizando LazyColumn
        LazyColumn {
            items(listaRegistros) { item ->
                RecordCard(
                    ambito = item.ambito,
                    detalle = item.detalle,
                    intensidad = item.intensidad,
                    fecha = item.fecha
                )
            }
        }
    }
}
