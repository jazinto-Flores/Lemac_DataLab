package com.example.autoregistroapp.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.autoregistroapp.MainActivity
import java.util.Calendar

/**
 * Recurso nativo: notificaciones locales programadas con AlarmManager.
 * El texto es neutro y la notificación es VISIBILITY_SECRET para no mostrar
 * contenido sensible en la pantalla bloqueada (requisito del caso).
 */
object ReminderScheduler {

    const val CHANNEL_ID = "recordatorios"
    const val EXTRA_HORA = "hora"
    const val EXTRA_MINUTO = "minuto"
    const val EXTRA_REPETIR = "repetir"

    private const val REQ_DIARIO = 1001
    private const val REQ_PRUEBA = 1002
    private const val NOTIF_ID = 2001

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Recordatorios discretos para tu autorregistro" }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    /** Programa el próximo recordatorio diario (el receiver programa el siguiente). */
    fun programar(context: Context, hora: Int, minuto: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent(context, REQ_DIARIO, hora, minuto, repetir = true)
        )
    }

    fun cancelar(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(pendingIntent(context, REQ_DIARIO, 0, 0, repetir = true))
    }

    /** Lanza una notificación de prueba dentro de 5 segundos (útil para la demo). */
    fun probarEnSegundos(context: Context, segundos: Int = 5) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + segundos * 1000L,
            pendingIntent(context, REQ_PRUEBA, 0, 0, repetir = false)
        )
    }

    @SuppressLint("MissingPermission")
    fun mostrarNotificacion(context: Context) {
        crearCanal(context)
        if (!tienePermiso(context)) return

        val abrirApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Lemac DataLab")
            .setContentText("Tienes un momento para tu registro de hoy.")
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID, notificacion)
        } catch (e: SecurityException) {
            // Permiso revocado: no se muestra nada
        }
    }

    private fun pendingIntent(
        context: Context, requestCode: Int, hora: Int, minuto: Int, repetir: Boolean
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_HORA, hora)
            putExtra(EXTRA_MINUTO, minuto)
            putExtra(EXTRA_REPETIR, repetir)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
