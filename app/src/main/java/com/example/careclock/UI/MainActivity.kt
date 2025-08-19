// app/src/main/java/com/example/careclock/ui/MainActivity.kt
package com.example.careclock.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.careclock.R
import com.example.careclock.models.Medication
import com.example.careclock.notifications.AlarmScheduler
import com.example.careclock.notifications.NotificationHelper
import com.example.careclock.storage.MedicationStorage
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MedicationAdapter
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            adapter.notifyDataSetChanged() // atualiza timers nos itens
            handler.postDelayed(this, 1000) // atualiza a cada segundo
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createChannel(this)

        val rv = findViewById<RecyclerView>(R.id.rvMeds)
        rv.layoutManager = LinearLayoutManager(this)

        val meds = MedicationStorage.load(this)
        adapter = MedicationAdapter(meds) { med, action ->
            when(action) {
                MedicationAdapter.Action.EDIT -> {
                    // abrir tela de edição (neste exemplo usamos a mesma AddMedicationActivity)
                    val i = Intent(this, AddMedicationActivity::class.java)
                    i.putExtra("medId", med.id)
                    startActivity(i)
                }
                MedicationAdapter.Action.DELETE -> {
                    AlarmScheduler.cancel(this, med)
                    meds.remove(med)
                    MedicationStorage.save(this, meds)
                    adapter.notifyDataSetChanged()
                }
            }
        }
        rv.adapter = adapter

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddMedicationActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }
}