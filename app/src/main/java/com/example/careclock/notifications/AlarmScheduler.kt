package com.example.careclock.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.careclock.models.Medication

object AlarmScheduler {

    const val EXTRA_TYPE = "alarm_type"
    const val TYPE_DOSE = "dose"
    const val TYPE_RENEWAL = "renewal"

    fun schedule(context: Context, med: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_DOSE) // Identifica como alarme de dose
            putExtra("medId", med.id)
            putExtra("medName", med.name)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            med.id.hashCode(), // ID único para o alarme de dose
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val first = med.nextDoseAfter(System.currentTimeMillis())
        if (first == Long.MAX_VALUE) return // Não agenda se o tratamento acabou

        val intervalMs = med.intervalMinutes * 60_000L

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, first, intervalMs, pending)
    }

    fun cancel(context: Context, med: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            med.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }

    // NOVA FUNÇÃO PARA AGENDAR O ALARME DE RENOVAÇÃO
    fun scheduleRenewal(context: Context, med: Medication) {
        med.renewalDateMillis ?: return // Sai se não houver data de renovação

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_TYPE, TYPE_RENEWAL) // Identifica como alarme de renovação
            putExtra("medId", med.id)
            putExtra("medName", med.name)
        }

        // Usamos um request code diferente para não sobrescrever o alarme de dose
        val requestCode = med.id.hashCode() + 1

        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Agenda um alarme único e exato
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, med.renewalDateMillis!!, pending)
    }

    // NOVA FUNÇÃO PARA CANCELAR O ALARME DE RENOVAÇÃO
    fun cancelRenewal(context: Context, med: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = med.id.hashCode() + 1
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pending)
    }
}