package com.example.fakestoreroles

/** Pantalla de acceso y navegación tras autenticar. */

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

/** Presenta el formulario y delega el acceso a LoginController. */
class LoginActivity : AppCompatActivity() {
    private val controller = LoginController()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SessionManager.getSession(this) != null) { openSession(); return }
        setContentView(R.layout.activity_login)
        applyStoreInsets(findViewById(android.R.id.content))
        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val login = findViewById<Button>(R.id.btnLogin)
        val progress = findViewById<ProgressBar>(R.id.progressLogin)
        val message = findViewById<TextView>(R.id.tvMessage)
        // Botón de acceso: valida, evita dobles envíos y entrega el resultado al controlador.
        login.setOnClickListener {
            val user = username.text.toString().trim()
            val pass = password.text.toString()
            username.error = LoginRules.username(user)
            password.error = LoginRules.password(pass)
            if (username.error != null || password.error != null) return@setOnClickListener
            if (!NetworkUtils.hasInternetConnection(this)) {
                message.text = "No hay conexión a Internet."; return@setOnClickListener
            }
            message.text = ""
            login.isEnabled = false
            username.isEnabled = false
            password.isEnabled = false
            progress.visibility = View.VISIBLE
            // Se cancela al destruir la pantalla; una respuesta vieja no guarda una sesión.
            lifecycleScope.launch {
                try {
                    val session = withContext(Dispatchers.IO) { controller.authenticate(user, pass) }
                    SessionManager.saveSession(this@LoginActivity, session)
                    openSession()
                } catch (e: CancellationException) { throw e }
                catch (e: ApiException) { message.text = e.message }
                catch (_: Exception) { message.text = "No se pudo iniciar sesión. Revisa la conexión y reintenta." }
                finally {
                    login.isEnabled = true
                    username.isEnabled = true
                    password.isEnabled = true
                    progress.visibility = View.GONE
                }
            }
        }
    }
    private fun openSession() {
        startActivity(Intent(this, CatalogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
