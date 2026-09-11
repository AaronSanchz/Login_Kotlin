package com.example.fakestoreroles

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ClientActivity : AppCompatActivity() {

    private lateinit var productsContainer: LinearLayout
    private lateinit var progress: ProgressBar
    private lateinit var message: TextView
    private lateinit var cartCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager.getSession(this)
        if (session == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_client)

        findViewById<TextView>(R.id.tvClientSession).text =
            "Sesión: ${session.username} | ID: ${session.userId}"

        productsContainer = findViewById(R.id.productsContainer)
        progress = findViewById(R.id.progressProducts)
        message = findViewById(R.id.tvClientMessage)
        cartCount = findViewById(R.id.tvCartCount)

        updateCartCount()
        findViewById<Button>(R.id.btnReloadProducts).setOnClickListener { loadProducts() }
        findViewById<Button>(R.id.btnLogoutClient).setOnClickListener { logout() }

        loadProducts()
    }

    private fun loadProducts() {
        progress.visibility = View.VISIBLE
        message.text = ""
        productsContainer.removeAllViews()

        Thread {
            try {
                val products = ApiService.getProducts()
                runOnUiThread {
                    progress.visibility = View.GONE
                    if (products.isEmpty()) {
                        message.text = "No hay productos para mostrar."
                    } else {
                        products.forEach { addProductView(it) }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    message.text = "No se pudieron cargar los productos."
                }
            }
        }.start()
    }

    private fun addProductView(product: ProductItem) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 16, 10, 16)
        }

        val textView = TextView(this).apply {
            text = buildString {
                append("${product.title}\n")
                append("Categoría: ${product.category}\n")
                append("Precio: \$${"%.2f".format(product.price)}")
            }
            textSize = 16f
        }

        val addButton = Button(this).apply {
            text = "Agregar al carrito"
            setOnClickListener {
                CartState.add(product.id)
                updateCartCount()
            }
        }

        row.addView(textView)
        row.addView(addButton)
        productsContainer.addView(row)

        val separator = View(this)
        separator.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1
        )
        separator.setBackgroundColor(0x22000000)
        productsContainer.addView(separator)
    }

    private fun updateCartCount() {
        cartCount.text = "Carrito local: ${CartState.count()} producto(s)"
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
