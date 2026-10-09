package viewModel
import androidx.lifecycle.ViewModel
import com.ejemplo.autodiario.model.Registro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegistroViewModel : ViewModel() {

    private val _registros = MutableStateFlow<List<Registro>>(emptyList())
    val registros: StateFlow<List<Registro>> = _registros

    init {
        // Cargar datos sintéticos de prueba
        _registros.value = listOf(
            Registro(ambito = "Estado de Ánimo", detalle = "Sensación de tranquilidad tras pausa", intensidad = 3, fecha = "10:30 AM"),
            Registro(ambito = "DBT", detalle = "Práctica de respiración profunda 4-7-8", intensidad = 2, fecha = "02:15 PM")
        )
    }

    fun agregarRegistro(ambito: String, detalle: String, intensidad: Int) {
        val nuevo = Registro(
            ambito = ambito,
            detalle = detalle,
            intensidad = intensidad,
            fecha = "Ahora"
        )
        _registros.value = _registros.value + nuevo
    }
}
