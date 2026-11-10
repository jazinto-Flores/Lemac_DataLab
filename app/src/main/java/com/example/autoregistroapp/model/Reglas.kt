package com.example.autoregistroapp.model

/** Criterios de valor crítico definidos por la contraparte (3.3). */
object Criticidad {
    fun esCritico(r: Registro): Boolean {
        if (r.eliminado) return false
        return when (r.ambito) {
            Ambito.ESTADO_ANIMO ->
                r.riesgoAlto || r.psicosis || (r.horasSueno != null && r.horasSueno < 3)
            Ambito.DBT, Ambito.ADICCIONES -> r.nivel == 4
            Ambito.NEURODESARROLLO -> false
        }
    }
}

/** Regla 1.1: la última modificación (UTC) gana, por registro. Empate: se queda el local. */
object Conflictos {
    fun ganador(local: Registro, remoto: Registro): Registro =
        if (remoto.modificadoUtc > local.modificadoUtc) remoto else local
}