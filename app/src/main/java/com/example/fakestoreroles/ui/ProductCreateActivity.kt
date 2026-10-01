// GUÍA DEL ARCHIVO: US06: bloquea apertura sin Administrador; carga categorías fuera del hilo visual. renderForm crea campos y valida localmente. POST confirmado limpia formulario y abre alerta con el ID devuelto.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** US06: vista de alta; el repositorio conserva las reglas de permiso y validación. */
class ProductCreateActivity : AppCompatActivity() {
    private lateinit var repository: ProductRepository
    private lateinit var root: LinearLayout
    private var saving = false

    /** Entrada del ciclo de vida Android: enlaza o construye vistas, revisa sesión cuando aplica y prepara callbacks de esta pantalla. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // También protege la apertura directa de esta Activity.
        if (SessionManager.getSession(this)?.role != UserRole.ADMINISTRADOR) {
            startActivity(android.content.Intent(this, CatalogActivity::class.java)); finish(); return
        }
        val app = applicationContext
        repository = HttpProductRepository({ SessionManager.getSession(app) })
        root = storeRoot("Agregar producto")
        loadCategories()
    }

    /** Las categorías vienen de la API y se usan para validar la selección. */
    /** Muestra carga, obtiene categorías en IO y construye formulario o botón de reintento. */
    private fun loadCategories() {
        root.removeAllViews()
        root.addView(ProgressBar(this))
        lifecycleScope.launch {
            try {
                val categories = withContext(Dispatchers.IO) { repository.categories() }
                if (categories.isEmpty()) throw ApiException("No hay categorías disponibles.")
                renderForm(categories)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                root.removeAllViews()
                root.addView(label(e.message ?: "No se pudieron cargar las categorías."))
                root.addView(action("Reintentar") { loadCategories() })
            }
        }
    }

    /** Construye el formulario y deja cada error junto al campo correspondiente. */
    /** Construye campos de creación, categoría, errores y botón Guardar. El callback valida antes de llamar create. */
    private fun renderForm(categories: List<String>) {
        root.removeAllViews()
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(12), dp(20), dp(12)) }
        root.addView(ScrollView(this).apply { addView(form) }, LinearLayout.LayoutParams(-1, 0, 1f))
        form.addView(label("La API confirma el alta, pero no conserva el producto.", 14f))
        /** Añade etiqueta y EditText al formulario; name nombra el campo y type configura teclado. */
        fun field(name: String, type: Int = InputType.TYPE_CLASS_TEXT): EditText {
            form.addView(label(name))
            return EditText(this).apply { inputType = type; contentDescription = name; form.addView(this) }
        }
        val title = field("Título")
        val price = field("Precio", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val description = field("Descripción", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        val image = field("Imagen (URL HTTPS)", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        form.addView(label("Categoría"))
        val category = Spinner(this).apply {
            adapter = ArrayAdapter(this@ProductCreateActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("Selecciona una categoría") + categories)
            form.addView(this)
        }
        val error = label("").apply { setTextColor(android.graphics.Color.RED) }; form.addView(error)
        val progress = ProgressBar(this).apply { visibility = View.GONE }; form.addView(progress)
        lateinit var save: Button
        save = action("Guardar") {
            if (saving) return@action
            title.error = ProductRules.text(title.text.toString())
            price.error = ProductRules.price(price.text.toString())
            description.error = ProductRules.text(description.text.toString(), 5000)
            image.error = ProductRules.image(image.text.toString())
            error.text = if (category.selectedItemPosition == 0) "Selecciona una categoría." else ""
            if (listOf(title, price, description, image).any { it.error != null } || category.selectedItemPosition == 0) return@action
            val candidate = Product(0, title.text.toString().trim(), ProductRules.parsePrice(price.text.toString())!!,
                description.text.toString().trim(), categories[category.selectedItemPosition - 1], image.text.toString().trim())
            saving = true; save.isEnabled = false; progress.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val created = withContext(Dispatchers.IO) { repository.create(candidate, categories) }
                    title.text.clear(); price.text.clear(); description.text.clear(); image.text.clear(); category.setSelection(0)
                    AlertDialog.Builder(this@ProductCreateActivity)
                        .setTitle("Producto creado (Simulación)")
                        .setMessage("Nuevo ID: ${created.id}. La API no conserva el producto.")
                        .setPositiveButton("Aceptar", null).show()
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { error.text = e.message ?: "No se pudo crear el producto." }
                finally { saving = false; save.isEnabled = true; progress.visibility = View.GONE }
            }
        }
        form.addView(save)
    }
}
