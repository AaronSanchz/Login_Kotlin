package com.example.fakestoreroles

/** Pantalla principal del cliente. */

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Adaptador de la ruta anterior al catálogo común con permisos de sesión. */
class ClientActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, CatalogActivity::class.java))
        finish()
    }
}
