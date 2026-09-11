package com.example.fakestoreroles

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedSession = SessionManager.getSession(this)
        if (savedSession != null) {
            openSession(savedSession)
            return
        }

        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString()

            if (username.isBlank() || password.isBlank()) {
                tvMessage.text = "Completa el usuario y la contraseña."
                return@setOnClickListener
            }

            // US01: se revisa la conectividad ANTES de consumir /auth/login.
            if (!NetworkUtils.hasInternetConnection(this)) {
                tvMessage.text = "No hay conexión a Internet."
                return@setOnClickListener
            }

            tvMessage.text = ""
            btnLogin.isEnabled = false
            progress.visibility = View.VISIBLE

            Thread {
                try {
                    val session = ApiService.authenticate(username, password)
                    SessionManager.saveSession(this, session)

                    runOnUiThread {
                        btnLogin.isEnabled = true
                        progress.visibility = View.GONE
                        openSession(session)
                    }
                } catch (e: ApiException) {
                    runOnUiThread {
                        btnLogin.isEnabled = true
                        progress.visibility = View.GONE
                        tvMessage.text = e.message ?: "No se pudo iniciar sesión."
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        btnLogin.isEnabled = true
                        progress.visibility = View.GONE
                        tvMessage.text = if (NetworkUtils.hasInternetConnection(this)) {
                            "No se pudo conectar con Fake Store API."
                        } else {
                            "No hay conexión a Internet."
                        }
                    }
                }
            }.start()
        }
    }

    private fun openSession(session: SessionData) {
        val destination = when (session.role) {
            UserRole.ADMINISTRADOR -> AdminActivity::class.java
            UserRole.AUDITOR -> AuditorActivity::class.java
            UserRole.CLIENTE -> ClientActivity::class.java
        }

        val intent = Intent(this, destination).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
