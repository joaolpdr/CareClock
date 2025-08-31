package com.example.careclock.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.careclock.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddDependentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_dependent)

        val etDependentName = findViewById<EditText>(R.id.etDependentName)
        val btnSaveDependent = findViewById<Button>(R.id.btnSaveDependent)

        btnSaveDependent.setOnClickListener {
            val dependentName = etDependentName.text.toString().trim()

            if (dependentName.isEmpty()) {
                etDependentName.error = "O nome é obrigatório."
                return@setOnClickListener
            }

            saveDependentToFirestore(dependentName)
        }
    }

    private fun saveDependentToFirestore(name: String) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            // Se não houver utilizador, não faz nada (idealmente, isto não deveria acontecer)
            Toast.makeText(this, "Erro: Nenhum utilizador logado.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = Firebase.firestore
        val dependentData = hashMapOf("name" to name)

        // Cria uma sub-coleção "dependents" dentro do documento do utilizador
        db.collection("users").document(user.uid)
            .collection("dependents")
            .add(dependentData)
            .addOnSuccessListener {
                Toast.makeText(this, "Dependente '$name' adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Fecha a tela e volta para a tela de Perfil
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao adicionar dependente: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}