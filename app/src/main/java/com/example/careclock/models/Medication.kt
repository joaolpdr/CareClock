package com.example.careclock.models

import java.util.*

data class Medication(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var startTimeMillis: Long = 0,
    var intervalMinutes: Long = 0,
    var durationDays: Int? = null,
    var active: Boolean = true,
    var renewalDateMillis: Long? = null
) {
    // Adiciona um construtor vazio que é necessário para o Firestore
    constructor() : this("", "", 0, 0, null, true, null)

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