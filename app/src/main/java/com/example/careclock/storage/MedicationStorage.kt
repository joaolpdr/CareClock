// app/src/main/java/com/example/careclock/storage/MedicationStorage.kt
package com.example.careclock.storage

import android.content.Context
import com.example.careclock.models.Medication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object MedicationStorage {
    private const val PREFS = "careclock_prefs"
    private const val KEY_MEDS = "meds"
    private val gson = Gson()

    fun load(context: Context): MutableList<Medication> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_MEDS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Medication>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(context: Context, list: List<Medication>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MEDS, gson.toJson(list)).apply()
    }
}