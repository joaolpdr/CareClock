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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    // Prepara o "lançador" do pedido de permissão
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permissão concedida.
            } else {
                // Permissão negada. Pode mostrar uma mensagem a explicar porque a permissão é importante.
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

    // Função para verificar e pedir a permissão
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Apenas para Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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