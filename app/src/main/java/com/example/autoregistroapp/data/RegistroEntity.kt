package com.example.autoregistroapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.autoregistroapp.model.Ambito
import com.example.autoregistroapp.model.EstadoObjetivo
import com.example.autoregistroapp.model.Registro

/** Los campos de texto (opcion, accion, notas) se guardan CIFRADOS. */
@Entity(tableName = "registros")
data class RegistroEntity(
    @PrimaryKey val id: String,
    val usuario: String,
    val fechaDia: String,
    val ambito: String,
    val nivel: Int,
    val nivelElevacion: Int,      // -1 = no aplica
    val horasSueno: Int,          // -1 = no aplica
    val riesgoAlto: Boolean,
    val psicosis: Boolean,
    val opcion: String,           // cifrado
    val accion: String,           // cifrado
    val estadoObjetivo: String,
    val notas: String,            // cifrado
    val modificadoUtc: Long,
    val eliminado: Boolean,
    val editado: Boolean,
    val pendienteSync: Boolean
)

/** Historial: nada se pierde sin rastro (4.3) ni se descarta sin guardar (1.1). */
@Entity(tableName = "versiones_registro")
data class VersionRegistroEntity(
    @PrimaryKey(autoGenerate = true) val vid: Long = 0,
    val registroId: String,
    val modificadoUtc: Long,
    val motivo: String,
    val contenido: String          // JSON cifrado de la versión anterior
)

fun RegistroEntity.aModelo(): Registro = Registro(
    usuario = usuario,
    fechaDia = fechaDia,
    ambito = runCatching { Ambito.valueOf(ambito) }.getOrDefault(Ambito.ESTADO_ANIMO),
    nivel = nivel,
    nivelElevacion = nivelElevacion.takeIf { it >= 0 },
    horasSueno = horasSueno.takeIf { it >= 0 },
    riesgoAlto = riesgoAlto,
    psicosis = psicosis,
    opcion = Cifrador.descifrar(opcion),
    accion = Cifrador.descifrar(accion),
    estadoObjetivo = runCatching { EstadoObjetivo.valueOf(estadoObjetivo) }.getOrNull(),
    notas = Cifrador.descifrar(notas),
    modificadoUtc = modificadoUtc,
    eliminado = eliminado,
    editado = editado,
    pendienteSync = pendienteSync
)

fun Registro.aEntidad(): RegistroEntity = RegistroEntity(
    id = id,
    usuario = usuario,
    fechaDia = fechaDia,
    ambito = ambito.name,
    nivel = nivel,
    nivelElevacion = nivelElevacion ?: -1,
    horasSueno = horasSueno ?: -1,
    riesgoAlto = riesgoAlto,
    psicosis = psicosis,
    opcion = Cifrador.cifrar(opcion),
    accion = Cifrador.cifrar(accion),
    estadoObjetivo = estadoObjetivo?.name ?: "",
    notas = Cifrador.cifrar(notas),
    modificadoUtc = modificadoUtc,
    eliminado = eliminado,
    editado = editado,
    pendienteSync = pendienteSync
)