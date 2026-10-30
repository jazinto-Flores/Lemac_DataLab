package com.example.autoregistroapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.mostrarNotificacion(context)
        if (intent.getBooleanExtra(ReminderScheduler.EXTRA_REPETIR, false)) {
            val hora = intent.getIntExtra(ReminderScheduler.EXTRA_HORA, 20)
            val minuto = intent.getIntExtra(ReminderScheduler.EXTRA_MINUTO, 0)
            ReminderScheduler.programar(context, hora, minuto) // siguiente día
        }
    }
}
