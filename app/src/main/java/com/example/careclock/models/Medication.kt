// app/src/main/java/com/example/careclock/models/Medication.kt
package com.example.careclock.models

import java.util.*

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var startTimeMillis: Long,
    var intervalMinutes: Long,
    var durationDays: Int? = null,
    var active: Boolean = true
) {
    fun nextDoseAfter(now: Long = System.currentTimeMillis()): Long {
        if (!active) return Long.MAX_VALUE
        var next = startTimeMillis
        if (now <= startTimeMillis) return startTimeMillis
        val diff = now - startTimeMillis
        val intervalMs = intervalMinutes * 60_000L
        val passed = diff / intervalMs
        next = startTimeMillis + (passed + 1) * intervalMs

        durationDays?.let { days ->
            val end = startTimeMillis + days * 24L * 60L * 60L * 1000L
            if (next > end) {
                active = false
                return Long.MAX_VALUE
            }
        }
        return next
    }
}