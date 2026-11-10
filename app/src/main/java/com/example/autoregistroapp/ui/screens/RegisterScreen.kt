package com.example.autoregistroapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.Catalogo
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.model.fechaLegible
import com.example.autoregistroapp.model.hoyIso
import com.example.autoregistroapp.ui.components.FilaSwitch
import com.example.autoregistroapp.ui.components.PrimaryButton
import com.example.autoregistroapp.ui.components.SelectorCatalogo
import com.example.autoregistroapp.ui.components.SelectorEscala
import com.example.autoregistroapp.viewmodel.RegistroFormState
import com.example.autoregistroapp.viewmodel.RegistroViewModel

/** Módulo independiente de un ámbito: registrar, corregir (hoy o una fecha del historial). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegistroViewModel,
    ambito: Ambito,
    fecha: String?,
    onRegistroGuardado: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.formState.collectAsStateWithLifecycle()
    val objetivo by viewModel.objetivo.collectAsStateWithLifecycle()
    var inicializado by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(ambito, fecha) {
        if (!inicializado) {
            viewModel.iniciar(ambito, fecha ?: hoyIso())
            inicializado = true
        }
    }
    LaunchedEffect(form.guardado) {
        if (form.guardado) {
            viewModel.consumirGuardado()
            onRegistroGuardado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((if (form.esEdicion) "Corregir · " else "") + ambito.etiqueta) },
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
            FormularioAmbito(viewModel, form, objetivo)
            PrimaryButton(
                text = if (form.esEdicion) "Guardar cambios" else "Guardar registro",
                onClick = { viewModel.guardarRegistro() }
            )
        }
    }
}

/** Contenido del formulario; lo usan el módulo y el flujo «Registro de hoy». */
@Composable
fun FormularioAmbito(viewModel: RegistroViewModel, form: RegistroFormState, objetivo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Fecha del registro: ${form.fecha.fechaLegible()}", style = MaterialTheme.typography.bodyMedium)

        when (form.ambito) {
            Ambito.ESTADO_ANIMO -> {
                SelectorEscala(
                    titulo = "Ánimo depresivo",
                    descripcion = "Según cuánto afectó tus actividades habituales (hogar, trabajo o estudio).",
                    valor = form.nivel, error = form.errores["nivel"], signo = "−",
                    onChange = viewModel::onNivel
                )
                SelectorEscala(
                    titulo = "Ánimo elevado",
                    descripcion = "Según cuánto afectó tus actividades habituales. Si tuviste ambos el mismo día, márcalos los dos.",
                    valor = form.nivelElevacion, error = form.errores["elevacion"], signo = "+",
                    onChange = viewModel::onElevacion
                )
                OutlinedTextField(
                    value = form.horasSueno,
                    onValueChange = { viewModel.onHoras(it.filter(Char::isDigit).take(2)) },
                    label = { Text("Horas de sueño (opcional)") },
                    singleLine = true,
                    isError = form.errores["sueno"] != null,
                    supportingText = { form.errores["sueno"]?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                FilaSwitch("Siento un riesgo alto", form.riesgoAlto, viewModel::onRiesgo)
                FilaSwitch("Tuve síntomas psicóticos", form.psicosis, viewModel::onPsicosis)
            }

            Ambito.NEURODESARROLLO -> {
                SelectorEscala(
                    titulo = "Nivel de dificultad hoy", descripcion = null,
                    valor = form.nivel, error = form.errores["nivel"], onChange = viewModel::onNivel
                )
                SelectorCatalogo(
                    label = "Situación *",
                    opciones = Catalogo.opciones[form.ambito].orEmpty(),
                    valor = form.opcion, otro = form.opcionOtro, error = form.errores["opcion"],
                    onValor = viewModel::onOpcion, onOtro = viewModel::onOpcionOtro
                )
            }

            Ambito.DBT -> {
                SelectorEscala(
                    titulo = "Intensidad de la emoción", descripcion = null,
                    valor = form.nivel, error = form.errores["nivel"], onChange = viewModel::onNivel
                )
                SelectorCatalogo(
                    label = "Emoción *",
                    opciones = Catalogo.opciones[form.ambito].orEmpty(),
                    valor = form.opcion, otro = form.opcionOtro, error = form.errores["opcion"],
                    onValor = viewModel::onOpcion, onOtro = viewModel::onOpcionOtro
                )
                SelectorCatalogo(
                    label = "Habilidad DBT usada (opcional)",
                    opciones = Catalogo.acciones[form.ambito].orEmpty(),
                    valor = form.accion, otro = form.accionOtro, error = form.errores["accion"],
                    onValor = viewModel::onAccion, onOtro = viewModel::onAccionOtro
                )
            }

            Ambito.ADICCIONES -> {
                SelectorEscala(
                    titulo = "Intensidad del craving", descripcion = null,
                    valor = form.nivel, error = form.errores["nivel"], onChange = viewModel::onNivel
                )
                SelectorCatalogo(
                    label = "Desencadenante (opcional)",
                    opciones = Catalogo.opciones[form.ambito].orEmpty(),
                    valor = form.opcion, otro = form.opcionOtro, error = form.errores["opcion"],
                    onValor = viewModel::onOpcion, onOtro = viewModel::onOpcionOtro
                )
                SelectorCatalogo(
                    label = "Acción protectora (opcional)",
                    opciones = Catalogo.acciones[form.ambito].orEmpty(),
                    valor = form.accion, otro = form.accionOtro, error = form.errores["accion"],
                    onValor = viewModel::onAccion, onOtro = viewModel::onAccionOtro
                )
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Tu objetivo personal (definido en tu perfil)", style = MaterialTheme.typography.labelLarge)
                        Text(objetivo.ifBlank { "Sin objetivo definido" }, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text("¿Cómo vas con tu objetivo hoy? *", style = MaterialTheme.typography.titleMedium)
                EstadoObjetivo.entries.forEach { e ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = form.estadoObjetivo == e,
                                onClick = { viewModel.onObjetivo(e) },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = form.estadoObjetivo == e, onClick = null)
                        Text(e.etiqueta, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                form.errores["objetivo"]?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        OutlinedTextField(
            value = form.notas,
            onValueChange = viewModel::onNotas,
            label = { Text("Notas (opcional)") },
            isError = form.errores["notas"] != null,
            supportingText = {
                Text(form.errores["notas"] ?: "${form.notas.length}/200 · No escribas datos personales reales")
            },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}