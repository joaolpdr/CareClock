package com.example.careclock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medId = intent.getStringExtra("medId") ?: return
        val medName = intent.getStringExtra("medName") ?: "Medicamento"
        val alarmType = intent.getStringExtra(AlarmScheduler.EXTRA_TYPE)

        if (alarmType == AlarmScheduler.TYPE_RENEWAL) {
            // Se for um alarme de renovação, mostra esta notificação
            NotificationHelper.showNotification(
                context,
                medId.hashCode() + 1, // ID diferente para a notificação
                "Hora de renovar a receita!",
                "Não se esqueça de ir ao médico para renovar a receita do seu $medName."
            )
        } else {
            // Caso contrário, é um alarme de dose normal
            NotificationHelper.showNotification(
                context,
                medId.hashCode(),
                "Hora do seu remédio!",
                "Não se esqueça de tomar o seu $medName."
            )
        }
    }
}