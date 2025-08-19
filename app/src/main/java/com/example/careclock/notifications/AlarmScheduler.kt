// app/src/main/java/com/example/careclock/notifications/AlarmScheduler.kt
package com.example.careclock.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.careclock.models.Medication

object AlarmScheduler {
    fun schedule(context: Context, med: Medication) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medId", med.id)
            putExtra("medName", med.name)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            med.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val first = med.nextDoseAfter(System.currentTimeMillis())
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
}