// GUÍA DEL ARCHIVO: Funciones de extensión para vistas nativas: dp convierte densidad, label crea texto, action botón con callback, storeRoot estructura vertical. Insets evitan que teclado y barras tapen los controles.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Componentes visuales compartidos de la aplicación. */

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Componentes de estilo compartidos; las dimensiones se expresan en dp. */
/** Convierte una medida lógica dp a píxeles según densidad del dispositivo; evita controles demasiado pequeños. */
fun android.content.Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
/** Crea TextView con texto, tamaño y color comunes; el llamador decide dónde insertarlo. */
fun android.content.Context.label(value: String, size: Float = 16f) = TextView(this).apply {
    text = value; textSize = size; setTextColor(Color.rgb(25, 45, 42)); setPadding(0, dp(6), 0, dp(6))
}
/** Crea un botón táctil accesible; cada pantalla aporta la acción autorizada. */
/** Crea botón accesible y conecta su pulsación con el callback onClick recibido; no decide permiso ni HTTP. */
fun android.content.Context.action(value: String, onClick: () -> Unit) = Button(this).apply {
    text = value; isAllCaps = false; minHeight = dp(48); setOnClickListener { onClick() }
}
/** Crea contenedor vertical con título, fondo y ajuste de barras/teclado; lo instala con setContentView. */
fun AppCompatActivity.storeRoot(title: String): LinearLayout {
    val root = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(8))
        setBackgroundColor(Color.rgb(245, 247, 246))
    }
    setContentView(root)
    // Protege contenido de las barras del sistema en Android con edge-to-edge.
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        view.setPadding(dp(16) + bars.left, dp(12) + bars.top, dp(16) + bars.right, dp(8) + bars.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(root)
    root.addView(label(title, 25f).apply { setTypeface(typeface, Typeface.BOLD) })
    return root
}
/** Devuelve fondo blanco redondeado con borde para las filas del catálogo. */
fun android.content.Context.cardBackground() = GradientDrawable().apply {
    setColor(Color.WHITE); cornerRadius = dp(16).toFloat(); setStroke(dp(1), Color.rgb(220, 230, 226))
}

/** Evita que los formularios XML queden debajo de las barras de Android 15. */
/** Aplica espacio de barras del sistema y teclado a la raíz de un layout XML. */
fun AppCompatActivity.applyStoreInsets(root: android.view.View) {
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
