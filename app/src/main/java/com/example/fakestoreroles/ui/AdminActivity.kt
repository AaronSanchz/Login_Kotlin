// GUÍA DEL ARCHIVO: Panel heredado del Administrador con activity_admin.xml. Comprueba sesión/rol antes de GET /users, crea filas informativas y permite recarga. logout borra sesión y carrito y elimina historial con CLEAR_TASK.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Pantalla y acciones disponibles para el administrador. */

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

/** Presenta el área de gestión del administrador. */
class AdminActivity : AppCompatActivity() {

    private var loading = false
    private lateinit var usersContainer: LinearLayout
    private lateinit var progress: ProgressBar
    private lateinit var message: TextView

    /** Entrada del ciclo de vida Android: enlaza o construye vistas, revisa sesión cuando aplica y prepara callbacks de esta pantalla. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager.getSession(this)
        if (session == null || session.role != UserRole.ADMINISTRADOR) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_admin)
        applyStoreInsets(findViewById(android.R.id.content))

        findViewById<TextView>(R.id.tvAdminSession).text =
            "Sesión: ${session.username} | ID: ${session.userId}"

        usersContainer = findViewById(R.id.usersContainer)
        progress = findViewById(R.id.progressUsers)
        message = findViewById(R.id.tvAdminMessage)

        findViewById<Button>(R.id.btnReloadUsers).setOnClickListener { loadUsers() }
        findViewById<Button>(R.id.btnLogoutAdmin).setOnClickListener { logout() }

        loadUsers()
    }

    /** Evita recargas duplicadas, solicita usuarios fuera del hilo visual y representa lista vacía o error. */
    private fun loadUsers() {
        if (loading) return
        loading = true
        val reload = findViewById<Button>(R.id.btnReloadUsers)
        reload.isEnabled = false
        progress.visibility = View.VISIBLE
        message.text = ""
        usersContainer.removeAllViews()
        lifecycleScope.launch {
            try {
                val users = withContext(Dispatchers.IO) { ApiService.getUsers() }
                if (users.isEmpty()) message.text = "No hay usuarios para mostrar."
                else users.forEach { addUserView(it) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { message.text = "No se pudieron cargar los usuarios. Reintenta." }
            finally {
                loading = false
                reload.isEnabled = true
                progress.visibility = View.GONE
            }
        }
    }
    /** Construye fila informativa de un UserItem: ID, nombre, usuario, correo y rol calculado localmente. */
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

    /** Borra preferencias de sesión, vacía carrito y abre Login con historial eliminado; informa fallo si no puede borrar. */
    private fun logout() {
        try { SessionManager.clearSession(this) } catch (_: Exception) { message.text = "No se pudo cerrar sesión. Reintenta."; return }
        CartState.clear()
        goToLogin()
    }

    /** Abre LoginActivity con NEW_TASK/CLEAR_TASK y finaliza la pantalla protegida. */
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
