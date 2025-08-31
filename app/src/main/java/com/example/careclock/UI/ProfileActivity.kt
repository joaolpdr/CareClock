package com.example.careclock.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.careclock.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var dependentsAdapter: DependentAdapter
    private val dependentsList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val rvDependents = findViewById<RecyclerView>(R.id.rvDependents)
        rvDependents.layoutManager = LinearLayoutManager(this)

        // ATUALIZAÇÃO AQUI: Passamos a lógica de clique para o adapter
        dependentsAdapter = DependentAdapter(dependentsList) { dependent ->
            // Isto será executado quando um dependente for clicado
            val intent = Intent(this, MainActivity::class.java).apply {
                // Passamos o ID e o nome do dependente para a MainActivity
                putExtra("DEPENDENT_ID", dependent["id"] as String)
                putExtra("DEPENDENT_NAME", dependent["name"] as String)
            }
            startActivity(intent)
        }
        rvDependents.adapter = dependentsAdapter

        loadUserProfile()

        findViewById<Button>(R.id.btnAddDependent).setOnClickListener {
            startActivity(Intent(this, AddDependentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadDependentsList()
    }

    private fun loadDependentsList() {
        val user = Firebase.auth.currentUser ?: return

        Firebase.firestore.collection("users").document(user.uid)
            .collection("dependents")
            .get()
            .addOnSuccessListener { documents ->
                dependentsList.clear()
                for (document in documents) {
                    val dependentData = document.data
                    dependentData["id"] = document.id
                    dependentsList.add(dependentData)
                }
                dependentsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("ProfileActivity", "Erro ao carregar dependentes.", exception)
            }
    }

    private fun loadUserProfile() {
        val user = Firebase.auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)

        val db = Firebase.firestore
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val fullName = document.getString("fullName")
                    val email = document.getString("email")
                    tvProfileName.text = "Nome: ${fullName ?: "Não informado"}"
                    tvProfileEmail.text = "Email: ${email ?: "Não informado"}"
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProfileActivity", "Erro ao obter dados do utilizador: ", exception)
            }
    }
}