package com.example.autoregistroapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.autoregistroapp.model.Catalogo
import com.example.autoregistroapp.model.Escala

/** Escala común 0–4: siempre muestra número y etiqueta (3.2). */
@Composable
fun SelectorEscala(
    titulo: String,
    descripcion: String?,
    valor: Int?,
    error: String?,
    signo: String = "",
    onChange: (Int) -> Unit
) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        descripcion?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (n in 0..4) {
                val sel = valor == n
                Surface(
                    onClick = { onChange(n) },
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            role = Role.RadioButton
                            this.selected = sel
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = if (sel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (n == 0) "0" else "$signo$n",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            Escala.etiqueta(n),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Catálogo cerrado + «Otro (especificar)» (4.1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorCatalogo(
    label: String,
    opciones: List<String>,
    valor: String,
    otro: String,
    error: String?,
    onValor: (String) -> Unit,
    onOtro: (String) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                isError = error != null && valor != Catalogo.OTRO,
                supportingText = { error?.takeIf { valor != Catalogo.OTRO }?.let { Text(it) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = { onValor(opcion); expandido = false }
                    )
                }
            }
        }
        if (valor == Catalogo.OTRO) {
            OutlinedTextField(
                value = otro,
                onValueChange = onOtro,
                label = { Text("Especifica") },
                singleLine = true,
                isError = error != null,
                supportingText = { error?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FilaSwitch(texto: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(texto, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun FilaCheck(texto: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(texto, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyMedium)
    }
}