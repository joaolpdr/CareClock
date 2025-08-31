package com.example.careclock.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.careclock.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        val db = Firebase.firestore // Inicializa o Firestore

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etDateOfBirth = findViewById<EditText>(R.id.etDateOfBirth)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etConfirmEmail = findViewById<EditText>(R.id.etConfirmEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val rgGender = findViewById<RadioGroup>(R.id.rgGender)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnNext.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val dob = etDateOfBirth.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val confirmEmail = etConfirmEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email != confirmEmail) {
                etConfirmEmail.error = "Os e-mails não coincidem."
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "As senhas não coincidem."
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "A senha deve ter no mínimo 6 caracteres."
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("RegisterActivity", "Utilizador criado com sucesso no Auth.")
                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser!!.uid

                        val selectedGenderId = rgGender.checkedRadioButtonId
                        val gender = if (selectedGenderId != -1) {
                            findViewById<RadioButton>(selectedGenderId).text.toString()
                        } else { "" }

                        // Cria um mapa com os dados do utilizador
                        val userProfile = hashMapOf(
                            "fullName" to fullName,
                            "dateOfBirth" to dob,
                            "phone" to phone,
                            "email" to email,
                            "gender" to gender
                        )

                        // Guarda os dados no Firestore
                        db.collection("users").document(userId)
                            .set(userProfile)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "Dados do utilizador guardados com sucesso no Firestore.")
                                Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("RegisterActivity", "Erro ao guardar dados do utilizador", e)
                                Toast.makeText(this, "Erro ao guardar perfil.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(
                            this, "Falha no registo: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}