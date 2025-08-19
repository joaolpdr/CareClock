// app/src/main/java/com/example/careclock/ui/LoginActivity.kt
package com.example.careclock.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.careclock.R
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEt = findViewById<EditText>(R.id.etEmail)
        val passEt = findViewById<EditText>(R.id.etPassword)
        val btn = findViewById<Button>(R.id.btnLogin)

        btn.setOnClickListener {
            // validação mínima — em app real valide e autentique em servidor
            val email = emailEt.text.toString().trim()
            if (email.isNotEmpty()) {
                // seguinte Activity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                emailEt.error = "Informe um e-mail"
            }
        }
    }
}