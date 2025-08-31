package com.example.careclock.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.careclock.R
import com.example.careclock.models.Medication
import com.example.careclock.notifications.AlarmScheduler
import com.example.careclock.notifications.NotificationHelper
import com.example.careclock.storage.MedicationStorage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var adapter: MedicationAdapter
    private val medsList = mutableListOf<Medication>()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var currentProfileId: String
    private lateinit var currentProfileName: String

    private val refreshRunnable = object : Runnable {
        override fun run() {
            adapter.notifyDataSetChanged()
            handler.postDelayed(this, 1000)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dependentId = intent.getStringExtra("DEPENDENT_ID")
        val dependentName = intent.getStringExtra("DEPENDENT_NAME")

        if (dependentId != null && dependentName != null) {
            currentProfileId = dependentId
            currentProfileName = dependentName
        } else {
            currentProfileId = Firebase.auth.currentUser!!.uid
            currentProfileName = "Você"
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        NotificationHelper.createChannel(this)
        askNotificationPermission()

        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val rvMeds = findViewById<RecyclerView>(R.id.rvMeds)
        setupRecyclerView(rvMeds) // Chama a nova função de configuração

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            val i = Intent(this, AddMedicationActivity::class.java).apply {
                putExtra("PROFILE_ID", currentProfileId)
            }
            startActivity(i)
        }
    }

    // NOVA FUNÇÃO PARA CONFIGURAR O RECYCLERVIEW E O GESTO DE ARRASTAR
    private fun setupRecyclerView(rvMeds: RecyclerView) {
        rvMeds.layoutManager = LinearLayoutManager(this)
        adapter = MedicationAdapter(medsList) { med, action ->
            when (action) {
                MedicationAdapter.Action.EDIT -> {
                    val i = Intent(this, AddMedicationActivity::class.java).apply {
                        putExtra("medId", med.id)
                        putExtra("PROFILE_ID", currentProfileId)
                    }
                    startActivity(i)
                }
                MedicationAdapter.Action.DELETE -> {
                    showDeleteConfirmationDialog(med)
                }
            }
        }
        rvMeds.adapter = adapter

        // Lógica do "Arrastar para o lado"
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // Não precisamos de mover/reordenar
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val medicationToDelete = medsList[position]
                showDeleteConfirmationDialog(medicationToDelete)
                // Notifica o adapter para re-desenhar o item na sua posição original
                // O item só será removido de facto se o utilizador confirmar no pop-up
                adapter.notifyItemChanged(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(rvMeds)
    }

    // LÓGICA DE APAGAR REUTILIZÁVEL
    private fun showDeleteConfirmationDialog(medication: Medication) {
        AlertDialog.Builder(this)
            .setTitle("Apagar Tratamento")
            .setMessage("Tem a certeza que quer apagar o tratamento com ${medication.name}?")
            .setPositiveButton("Sim") { _, _ ->
                AlarmScheduler.cancel(this, medication)
                AlarmScheduler.cancelRenewal(this, medication)
                MedicationStorage.delete(medication.id, currentProfileId) { success ->
                    if (success) {
                        Toast.makeText(this, "${medication.name} apagado.", Toast.LENGTH_SHORT).show()
                        updateMedicationList()
                    } else {
                        Toast.makeText(this, "Erro ao apagar.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
        updateMedicationList()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Configurações clicadas", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                showLogoutDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Terminar Sessão")
            .setMessage("Tem a certeza que quer sair da sua conta?")
            .setPositiveButton("Sim") { _, _ ->
                Firebase.auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun loadUserProfile() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        if (currentProfileId == user.uid) {
            val db = Firebase.firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName")
                        val firstName = fullName?.split(" ")?.firstOrNull() ?: "User"
                        tvWelcome.text = "Bem vindo(a), $firstName!"
                        val navigationView = findViewById<NavigationView>(R.id.nav_view)
                        val headerView = navigationView.getHeaderView(0)
                        headerView.findViewById<TextView>(R.id.headerUsername).text = fullName
                        headerView.findViewById<TextView>(R.id.headerUserEmail).text = user.email
                    }
                }
        } else {
            tvWelcome.text = "Medicamentos de: $currentProfileName"
        }
    }

    private fun updateMedicationList() {
        MedicationStorage.load(currentProfileId) { loadedMeds ->
            medsList.clear()
            medsList.addAll(loadedMeds)
            adapter.notifyDataSetChanged()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}