package com.example.careclock.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.careclock.R
import com.example.careclock.models.Medication
import com.example.careclock.notifications.AlarmScheduler
import com.example.careclock.storage.MedicationStorage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddMedicationActivity : AppCompatActivity() {

    private var editingMedId: String? = null
    private var chosenTime = Calendar.getInstance()
    private var chosenRenewTime = Calendar.getInstance()
    private var medicationBeingEdited: Medication? = null
    private lateinit var profileIdForMedication: String // ID do perfil para quem o medicamento se destina

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_medication)

        // Obtém para quem é este medicamento
        profileIdForMedication = intent.getStringExtra("PROFILE_ID") ?: Firebase.auth.currentUser!!.uid

        val etName = findViewById<EditText>(R.id.etName)
        val tvStartDate = findViewById<TextView>(R.id.tvStartDate)
        val rgDuration = findViewById<RadioGroup>(R.id.rgDuration)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etInterval = findViewById<EditText>(R.id.etInterval)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val layoutRenew = findViewById<LinearLayout>(R.id.layoutRenew)
        val cbRenew = findViewById<CheckBox>(R.id.cbRenew)
        val tvRenewDate = findViewById<TextView>(R.id.tvRenewDate)

        rgDuration.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbWithEndDate) {
                etDuration.visibility = View.VISIBLE
                layoutRenew.visibility = View.GONE
                cbRenew.isChecked = false
            } else {
                etDuration.visibility = View.GONE
                layoutRenew.visibility = View.VISIBLE
                etDuration.text.clear()
            }
        }

        cbRenew.setOnCheckedChangeListener { _, isChecked ->
            tvRenewDate.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        tvStartDate.setOnClickListener {
            showDateTimePicker(chosenTime, tvStartDate, "Início: %s")
        }
        tvRenewDate.setOnClickListener {
            showDateTimePicker(chosenRenewTime, tvRenewDate, "Renovar em: %s")
        }

        loadEditingMedicationData()

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val intervalHours = etInterval.text.toString().toLongOrNull() ?: 0L
            val durationDays = if (etDuration.visibility == View.VISIBLE) {
                etDuration.text.toString().toIntOrNull()
            } else { null }

            val renewalDate = if (cbRenew.isChecked) {
                chosenRenewTime.timeInMillis
            } else { null }

            if (name.isEmpty() || intervalHours <= 0) {
                Toast.makeText(this, "Preencha o nome e o intervalo das doses.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intervalMinutes = intervalHours * 60

            saveMedication(name, chosenTime.timeInMillis, intervalMinutes, durationDays, renewalDate)
        }
    }

    private fun showDateTimePicker(calendar: Calendar, textView: TextView, format: String) {
        val currentCalendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            TimePickerDialog(this, { _, hourOfDay, minute ->
                calendar.set(year, month, dayOfMonth, hourOfDay, minute)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                textView.text = String.format(format, sdf.format(calendar.time))
                textView.setTextColor(resources.getColor(android.R.color.black, null))
            }, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), true).show()
        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun saveMedication(name: String, startTime: Long, intervalMinutes: Long, durationDays: Int?, renewalDate: Long?) {
        val medicationToSave: Medication

        if (medicationBeingEdited != null) {
            medicationToSave = medicationBeingEdited!!
            medicationToSave.name = name
            medicationToSave.startTimeMillis = startTime
            medicationToSave.intervalMinutes = intervalMinutes
            medicationToSave.durationDays = durationDays
            medicationToSave.renewalDateMillis = renewalDate
            AlarmScheduler.cancel(this, medicationToSave)
            AlarmScheduler.cancelRenewal(this, medicationToSave)
        } else {
            medicationToSave = Medication(
                id = "",
                name = name,
                startTimeMillis = startTime,
                intervalMinutes = intervalMinutes,
                durationDays = durationDays,
                renewalDateMillis = renewalDate
            )
        }

        MedicationStorage.save(medicationToSave, profileIdForMedication) { success ->
            if (success) {
                AlarmScheduler.schedule(this, medicationToSave)
                if (renewalDate != null) {
                    AlarmScheduler.scheduleRenewal(this, medicationToSave)
                }
                Toast.makeText(this, "Tratamento salvo!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao salvar tratamento.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEditingMedicationData() {
        editingMedId = intent.getStringExtra("medId")
        if (editingMedId == null) return

        MedicationStorage.load(profileIdForMedication) { meds ->
            val med = meds.find { it.id == editingMedId }
            if (med != null) {
                medicationBeingEdited = med

                val etName = findViewById<EditText>(R.id.etName)
                val etInterval = findViewById<EditText>(R.id.etInterval)
                val etDuration = findViewById<EditText>(R.id.etDuration)
                val tvStartDate = findViewById<TextView>(R.id.tvStartDate)
                val cbRenew = findViewById<CheckBox>(R.id.cbRenew)
                val tvRenewDate = findViewById<TextView>(R.id.tvRenewDate)
                val rgDuration = findViewById<RadioGroup>(R.id.rgDuration)

                etName.setText(med.name)
                etInterval.setText((med.intervalMinutes / 60).toString())

                if (med.durationDays != null) {
                    rgDuration.check(R.id.rbWithEndDate)
                    etDuration.setText(med.durationDays.toString())
                } else {
                    rgDuration.check(R.id.rbIndeterminate)
                }

                med.renewalDateMillis?.let { renewalMillis ->
                    cbRenew.isChecked = true
                    chosenRenewTime.timeInMillis = renewalMillis
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    tvRenewDate.text = String.format("Renovar em: %s", sdf.format(chosenRenewTime.time))
                    tvRenewDate.setTextColor(resources.getColor(android.R.color.black, null))
                }

                chosenTime.timeInMillis = med.startTimeMillis
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvStartDate.text = String.format("Início: %s", sdf.format(chosenTime.time))
                tvStartDate.setTextColor(resources.getColor(android.R.color.black, null))
            }
        }
    }
}