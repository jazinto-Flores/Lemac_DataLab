package com.example.autoregistroapp.model

object TextosLegales {
    const val VERSION = "1.3"

    val noMonitoreada = listOf(
        "Nadie revisa tus registros en tiempo real.",
        "Un registro, una alerta o una señal de riesgo no notifica de forma inmediata a un médico, psicólogo, familiar ni al centro.",
        "No debes esperar que alguien te contacte, llame o intervenga a partir de lo que anotes.",
        "Si un registro sugiere riesgo, la app te mostrará ayuda inmediata y recursos de emergencia, pero no promete seguimiento clínico."
    )

    const val EMERGENCIA =
        "Si estás en peligro o necesitas ayuda ahora, deja esta app y llama al *4141, a Salud Responde 600 360 7777 o a emergencias 131."

    val secciones: List<Pair<String, String>> = listOf(
        "Quién trata tus datos" to "Lemac DataLab SpA es responsable del tratamiento de los datos que ingreses en esta app. Este MVP se usa solo con datos ficticios.",
        "Qué datos tratamos" to "Tu registro diario en cuatro ámbitos (estado de ánimo, neurodesarrollo, habilidades DBT y adicciones): niveles de 0 a 4, horas de sueño, señales de riesgo que tú marcas, opciones del catálogo y notas.",
        "Para qué" to "Solo para que lleves tu registro y veas tu evolución. La app no emite diagnósticos ni reemplaza la atención profesional.",
        "Datos sensibles" to "Los datos de salud son sensibles según la legislación chilena (Ley N° 19.628 y Ley N° 21.719). Solo los tratamos con tu consentimiento expreso.",
        "Seguridad" to "Tus registros se guardan cifrados en este dispositivo (AES-256) y tu sesión se cierra sola tras 5 minutos de inactividad.",
        "Con quién se comparten" to "Con nadie, salvo que lo exija la ley. El uso estadístico o de investigación es opcional y siempre sin tu nombre ni textos que escribas.",
        "Tus derechos" to "Puedes revisar este consentimiento, cambiar las autorizaciones opcionales y corregir o eliminar tus registros desde la app.",
        "Conservación" to "Tus datos se conservan mientras tu cuenta esté activa."
    )

    const val CHECK_DATOS = "Acepto el tratamiento de mis datos personales para llevar mi registro diario, según el texto anterior. (obligatorio)"
    const val CHECK_SALUD = "Autorizo expresamente el tratamiento de mis datos de salud (datos sensibles). (obligatorio)"
    const val CHECK_NO_MONITOREADA = "Entiendo que esta es una app de autoayuda no monitoreada: nadie revisa mis registros en tiempo real ni se avisará de inmediato a un médico si aparece una señal de riesgo. Si necesito ayuda urgente, buscaré atención inmediata. (obligatorio)"
    const val CHECK_ESTUDIOS = "Autorizo que mis registros se usen en estudios estadísticos o de investigación, sin mi nombre, correo, dirección ni textos que yo escriba. Puedo retirarla cuando quiera."
}