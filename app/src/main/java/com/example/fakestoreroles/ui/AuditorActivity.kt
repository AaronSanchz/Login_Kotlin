// GUÍA DEL ARCHIVO: Adaptador de compatibilidad de ruta antigua del Auditor. Redirige a CatalogActivity y finaliza; permisos y acciones se resuelven en el catálogo común.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Pantalla de consulta del auditor. */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Adaptador de la ruta anterior al catálogo común con permisos de sesión. */
class AuditorActivity : AppCompatActivity() {
    /** Entrada del ciclo de vida Android: enlaza o construye vistas, revisa sesión cuando aplica y prepara callbacks de esta pantalla. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, CatalogActivity::class.java))
        finish()
    }
}
