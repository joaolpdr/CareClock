// app/src/main/java/com/example/careclock/notifications/BootReceiver.kt
package com.example.careclock.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Toast.makeText(context, "Boot completed - re-agendar alarmes (implementar)", Toast.LENGTH_LONG).show()
        }
    }
}