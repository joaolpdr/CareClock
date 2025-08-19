// app/src/main/java/com/example/careclock/ui/AddMedicationActivity.kt
package com.example.careclock.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.careclock.R
import com.example.careclock.models.Medication
import com.example.careclock.notifications.AlarmScheduler
import com.example.careclock.storage.MedicationStorage
import java.util.*

class AddMedicationActivity : AppCompatActivity() {

    private var editingMedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        val etName = findViewById<EditText>(R.id.etName)
        val etInterval = findViewById<EditText>(R.id.etInterval)
        val btnPick = findViewById<Button>(R.id.btnPickDateTime)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val c = Calendar.getInstance()
        var chosenTime = c.timeInMillis

        btnPick.setOnClickListener {
            // primeiro date picker
            val dp = DatePickerDialog(this, { _, y, m, d ->
                val tp = TimePickerDialog(this, { _, hour, minute ->
                    val cal = Calendar.getInstance()
                    cal.set(y, m, d, hour, minute, 0)
                    chosenTime = cal.timeInMillis
                    btnPick.text = "Início: ${Date(chosenTime)}"
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true)
                tp.show()
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            dp.show()
        }

        // se veio medId, estamos editando
        editingMedId = intent.getStringExtra("medId")
        val meds = MedicationStorage.load(this)
        editingMedId?.let { id ->
            val med = meds.find { it.id == id }
            med?.let {
                etName.setText(it.name)
                etInterval.setText(it.intervalMinutes.toString())
                chosenTime = it.startTimeMillis
                btnPick.text = "Início: ${Date(chosenTime)}"
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val interval = etInterval.text.toString().toLongOrNull() ?: 0L
            if (name.isEmpty() || interval <= 0) {
                etName.error = "Preencha nome e intervalo"
                return@setOnClickListener
            }

            if (editingMedId != null) {
                val med = meds.find { it.id == editingMedId }
                med?.let {
                    it.name = name
                    it.intervalMinutes = interval
                    it.startTimeMillis = chosenTime
                    MedicationStorage.save(this, meds)
                    AlarmScheduler.cancel(this, it)
                    AlarmScheduler.schedule(this, it)
                }
            } else {
                val newMed = Medication(
                    name = name,
                    startTimeMillis = chosenTime,
                    intervalMinutes = interval
                )
                meds.add(newMed)
                MedicationStorage.save(this, meds)
                AlarmScheduler.schedule(this, newMed)
            }
            finish()
        }
    }
}