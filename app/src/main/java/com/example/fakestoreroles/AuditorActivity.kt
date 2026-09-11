package com.example.fakestoreroles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AuditorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager.getSession(this)
        if (session == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_auditor)

        findViewById<TextView>(R.id.tvAuditorSession).text =
            "Usuario: ${session.username}\nID: ${session.userId}\nRol: Auditor"

        findViewById<Button>(R.id.btnLogoutAuditor).setOnClickListener {
            SessionManager.clearSession(this)
            CartState.clear()
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
