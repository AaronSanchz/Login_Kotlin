package com.example.fakestoreroles

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class CatalogActivity : AppCompatActivity() {
    private lateinit var model: CatalogViewModel
    private val detail = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) model.load()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager.getSession(this) ?: run { goLogin(); return }
        val app = applicationContext
        val repo = HttpProductRepository({ SessionManager.getSession(app) })
        model = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CatalogViewModel(repo) as T
        })[CatalogViewModel::class.java]
        val root = storeRoot("Fake Store")
        root.addView(label("${session.username} · ${session.role.label}"))
        val actions = LinearLayout(this)
        actions.addView(action("Actualizar") { model.load(model.state.value.selected) }, LinearLayout.LayoutParams(0,-2,1f))
        actions.addView(action("Salir") {
            try { SessionManager.clearSession(this); CartState.clear(); goLogin() }
            catch (_: Exception) { Toast.makeText(this,"No se pudo cerrar sesión.",Toast.LENGTH_LONG).show() }
        }, LinearLayout.LayoutParams(0,-2,1f))
        if (session.role == UserRole.ADMINISTRADOR) actions.addView(action("Usuarios") { startActivity(Intent(this, AdminActivity::class.java)) }, LinearLayout.LayoutParams(0,-2,1f))
        root.addView(actions)
        val categoriesProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { isIndeterminate = true }
        root.addView(categoriesProgress, LinearLayout.LayoutParams(-1, dp(4)))
        val categoryRetry = action("Reintentar categorías") { model.loadCategories() }; root.addView(categoryRetry)
        val categoryScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val categoryRow = LinearLayout(this); categoryScroll.addView(categoryRow); root.addView(categoryScroll)
        val frame = FrameLayout(this); root.addView(frame, LinearLayout.LayoutParams(-1,0,1f))
        val adapter = ProductAdapter { p -> detail.launch(Intent(this, ProductDetailActivity::class.java).putExtra("id", p.id)) }
        val list = RecyclerView(this).apply { layoutManager = LinearLayoutManager(this@CatalogActivity); this.adapter = adapter }
        frame.addView(list, FrameLayout.LayoutParams(-1,-1))
        val progress = ProgressBar(this); frame.addView(progress, FrameLayout.LayoutParams(dp(48),dp(48),Gravity.CENTER))
        val status = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        val message = label(""); message.gravity = Gravity.CENTER; status.addView(message)
        val retry = action("Reintentar") { model.load(model.state.value.selected) }; status.addView(retry)
        frame.addView(status, FrameLayout.LayoutParams(-1,-2,Gravity.CENTER))
        var lastCategories: List<String>? = null; var lastSelected: String? = ""
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    categoriesProgress.visibility = if(state.categoriesLoading) View.VISIBLE else View.GONE
                    categoryRetry.visibility = if(state.categoryError != null) View.VISIBLE else View.GONE
                    if (lastCategories != state.categories || lastSelected != state.selected) {
                        categoryRow.removeAllViews()
                        fun addCategory(title: String, value: String?) {
                            categoryRow.addView(action(if(state.selected == value) "✓ $title" else title) {
                                model.load(if(state.selected == value) null else value)
                            }.apply { isSelected = state.selected == value })
                        }
                        addCategory("Ver todos", null); state.categories.forEach { addCategory(it,it) }
                        lastCategories = state.categories; lastSelected = state.selected
                    }
                    // Ocultar de inmediato evita ver filas antiguas mientras DiffUtil procesa la lista vacía.
                    list.visibility = if(state.loading || state.error != null) View.GONE else View.VISIBLE
                    adapter.submitList(state.products)
                    progress.visibility = if(state.loading) View.VISIBLE else View.GONE
                    status.visibility = if(!state.loading && (state.error != null || state.products.isEmpty())) View.VISIBLE else View.GONE
                    message.text = state.error ?: "No hay productos en esta categoría."
                    retry.visibility = if(state.error != null) View.VISIBLE else View.GONE
                }
            }
        }
    }
    private fun goLogin() {
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)); finish()
    }
}
