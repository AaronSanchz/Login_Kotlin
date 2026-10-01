// GUÍA DEL ARCHIVO: Adaptador de compatibilidad de ruta antigua. onCreate abre CatalogActivity y termina esta Activity; el catálogo valida sesión y construye la interfaz según el perfil almacenado.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Pantalla principal del cliente. */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Adaptador de la ruta anterior al catálogo común con permisos de sesión. */
class ClientActivity : AppCompatActivity() {
    /** Entrada del ciclo de vida Android: enlaza o construye vistas, revisa sesión cuando aplica y prepara callbacks de esta pantalla. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, CatalogActivity::class.java))
        finish()
    }
}
