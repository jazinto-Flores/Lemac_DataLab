package com.example.autoregistroapp.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Registro
import com.example.autoregistroapp.model.fechaIso

/**
 * Un gráfico por ámbito (5.1). En ánimo: dos líneas (depresión −4..0 y elevación 0..+4), sin combinarlas.
 * Un día sin registro queda SIN punto: nunca se dibuja como «sin síntomas» (valor 0).
 */
@Composable
fun GraficoEvolucion(
    ambito: Ambito,
    registros: List<Registro>,
    dias: Int,
    modifier: Modifier = Modifier
) {
    val animo = ambito == Ambito.ESTADO_ANIMO
    val fechas = remember(dias) { (dias - 1 downTo 0).map { fechaIso(it) } }
    val porFecha = remember(registros, ambito) {
        registros.filter { it.ambito == ambito && !it.eliminado }.associateBy { it.fechaDia }
    }
    val serieA: List<Float?> = fechas.map { f ->
        porFecha[f]?.let { if (animo) -it.nivel.toFloat() else it.nivel.toFloat() }
    }
    val serieB: List<Float?> =
        if (animo) fechas.map { f -> porFecha[f]?.nivelElevacion?.toFloat() } else emptyList()

    val minY = if (animo) -4f else 0f
    val maxY = 4f
    val colorEje = MaterialTheme.colorScheme.outline
    val colorA = if (animo) Color(0xFF2563EB) else MaterialTheme.colorScheme.primary
    val colorB = Color(0xFFEA580C)
    val conRegistro = fechas.count { porFecha.containsKey(it) }

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val w = size.width
            val h = size.height
            fun x(i: Int) = if (fechas.size == 1) w / 2f else w * i / (fechas.size - 1)
            fun y(v: Float) = h - (v - minY) / (maxY - minY) * h

            for (v in minY.toInt()..maxY.toInt()) {
                val destacada = animo && v == 0
                drawLine(
                    color = colorEje.copy(alpha = if (destacada) 0.8f else 0.25f),
                    start = Offset(0f, y(v.toFloat())),
                    end = Offset(w, y(v.toFloat())),
                    strokeWidth = 1.dp.toPx()
                )
            }

            fun dibujar(valores: List<Float?>, color: Color) {
                valores.forEachIndexed { i, v ->
                    if (v == null) return@forEachIndexed
                    val anterior = valores.getOrNull(i - 1)
                    if (anterior != null) {
                        drawLine(color, Offset(x(i - 1), y(anterior)), Offset(x(i), y(v)), strokeWidth = 3.dp.toPx())
                    }
                    drawCircle(color, radius = 4.dp.toPx(), center = Offset(x(i), y(v)))
                }
            }
            dibujar(serieA, colorA)
            if (animo) dibujar(serieB, colorB)
        }
        if (animo) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("● Depresión (−4 a 0)", color = colorA, style = MaterialTheme.typography.labelSmall)
                Text("● Elevación (0 a +4)", color = colorB, style = MaterialTheme.typography.labelSmall)
            }
        } else {
            Text("Escala 0 (sin síntomas) a 4 (severa)", style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "Días con registro: $conRegistro de $dias. Un día sin punto no tiene registro (no significa «sin síntomas»).",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/** Aviso dentro de la app por valor crítico (3.3). No bloquea; los botones solo abren el marcador. */
@Composable
fun AvisoCritico(onCerrar: () -> Unit) {
    val context = LocalContext.current
    fun marcar(numero: String) {
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(numero))))
        } catch (e: ActivityNotFoundException) {
            // Dispositivo sin marcador (p. ej. algunos emuladores)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Tu registro quedó guardado", style = MaterialTheme.typography.titleMedium)
            Text(
                "Lamentamos que estés pasando por un momento difícil. No estás solo/a y puedes pedir apoyo.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Esta app no es un servicio de urgencias y nadie revisa tus registros en tiempo real.",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Algo que puedes probar ahora: una habilidad DBT, como respirar lento durante 5 minutos " +
                        "nombrando lo que sientes, o una acción protectora, como salir a caminar.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(onClick = { marcar("+56900000000") }, modifier = Modifier.fillMaxWidth()) {
                Text("Llamar a mi contacto de apoyo (ficticio)")
            }
            OutlinedButton(onClick = { marcar("*4141") }, modifier = Modifier.fillMaxWidth()) {
                Text("*4141 · Prevención del suicidio")
            }
            OutlinedButton(onClick = { marcar("6003607777") }, modifier = Modifier.fillMaxWidth()) {
                Text("600 360 7777 · Salud Responde")
            }
            OutlinedButton(onClick = { marcar("131") }, modifier = Modifier.fillMaxWidth()) {
                Text("131 · SAMU")
            }
            TextButton(onClick = onCerrar) { Text("Entendido") }
        }
    }
}