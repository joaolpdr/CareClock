package com.example.careclock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // mostrar a notificação quando o alarme disparar
        val medId = intent.getStringExtra("medId") ?: return
        val medName = intent.getStringExtra("medName") ?: "Medicamento"

        NotificationHelper.showNotification(
            context,
            medId.hashCode(),
            "Hora do seu remédio!",
            "Não se esqueça de tomar o seu $medName."
        )
    }
}