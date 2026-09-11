package com.example.fakestoreroles

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    private lateinit var usersContainer: LinearLayout
    private lateinit var progress: ProgressBar
    private lateinit var message: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager.getSession(this)
        if (session == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_admin)

        findViewById<TextView>(R.id.tvAdminSession).text =
            "Sesión: ${session.username} | ID: ${session.userId}"

        usersContainer = findViewById(R.id.usersContainer)
        progress = findViewById(R.id.progressUsers)
        message = findViewById(R.id.tvAdminMessage)

        findViewById<Button>(R.id.btnReloadUsers).setOnClickListener { loadUsers() }
        findViewById<Button>(R.id.btnLogoutAdmin).setOnClickListener { logout() }

        loadUsers()
    }

    private fun loadUsers() {
        progress.visibility = View.VISIBLE
        message.text = ""
        usersContainer.removeAllViews()

        Thread {
            try {
                val users = ApiService.getUsers()
                runOnUiThread {
                    progress.visibility = View.GONE
                    if (users.isEmpty()) {
                        message.text = "No hay usuarios para mostrar."
                    } else {
                        users.forEach { addUserView(it) }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    message.text = "No se pudieron cargar los usuarios."
                }
            }
        }.start()
    }

    private fun addUserView(user: UserItem) {
        val textView = TextView(this)
        val fullName = "${user.firstName} ${user.lastName}".trim().ifBlank { "Usuario" }
        val role = ApiService.roleFromId(user.id).label

        textView.text = buildString {
            append("#${user.id} - $fullName\n")
            append("Usuario: ${user.username}\n")
            append("Correo: ${user.email}\n")
            append("Rol local: $role")
        }
        textView.textSize = 16f
        textView.setPadding(10, 16, 10, 16)
        usersContainer.addView(textView)

        val separator = View(this)
        separator.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        separator.setBackgroundColor(0x22000000)
        usersContainer.addView(separator)
    }

    private fun logout() {
        SessionManager.clearSession(this)
        CartState.clear()
        goToLogin()
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
