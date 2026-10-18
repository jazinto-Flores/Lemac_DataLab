package viewModel

import com.example.autoregistroapp.model.Ambito

data class RegistroFormState(
    val ambito: Ambito = Ambito.ESTADO_ANIMO,
    val emocionOImpulso: String = "",
    val emocionError: String? = null,
    val intensidad: Float = 3f,
    val intensidadError: String? = null,
    val contexto: String = "",
    val contextoError: String? = null,
    val notas: String = "",
    val notasError: String? = null,
    val isFormValid: Boolean = false,
    val isSuccess: Boolean = false
)
