package com.example.fakestoreroles

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Componentes de estilo compartidos; las dimensiones se expresan en dp. */
fun android.content.Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun android.content.Context.label(value: String, size: Float = 16f) = TextView(this).apply {
    text = value; textSize = size; setTextColor(Color.rgb(25, 45, 42)); setPadding(0, dp(6), 0, dp(6))
}
fun android.content.Context.action(value: String, onClick: () -> Unit) = Button(this).apply {
    text = value; isAllCaps = false; minHeight = dp(48); setOnClickListener { onClick() }
}
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
fun android.content.Context.cardBackground() = GradientDrawable().apply {
    setColor(Color.WHITE); cornerRadius = dp(16).toFloat(); setStroke(dp(1), Color.rgb(220, 230, 226))
}
