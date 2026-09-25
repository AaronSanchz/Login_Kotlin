package com.example.fakestoreroles

/** Detalle y controles según rol para carrito, edición y eliminación. */

import android.os.Bundle
import android.widget.*
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.util.Locale

/** Presenta detalle y controla acciones según el rol. */
class ProductDetailActivity : AppCompatActivity() {
    private lateinit var repository: ProductRepository
    private lateinit var root: LinearLayout
    private var product: Product? = null
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = storeRoot("Detalle del producto")
        root.addView(ProgressBar(this))
        val app = applicationContext
        repository = HttpProductRepository({ SessionManager.getSession(app) })
        val id = intent.getIntExtra("id", -1)
        loadProduct(id)
    }
    private fun loadProduct(id: Int) {
        root = storeRoot("Detalle del producto")
        root.addView(ProgressBar(this))
        lifecycleScope.launch {
            try {
                if (SessionManager.getSession(this@ProductDetailActivity) == null) {
                    startActivity(android.content.Intent(this@ProductDetailActivity, LoginActivity::class.java)
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    finish(); return@launch
                }
                product = withContext(Dispatchers.IO) { repository.detail(id) }
                render()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                root = storeRoot("Detalle del producto")
                root.addView(label(e.message ?: "No se pudo cargar el producto."))
                root.addView(action("Reintentar") { loadProduct(id) })
                root.addView(action("Volver al catálogo") { finish() })
            }
        }
    }
    private fun render() {
        val p = product ?: return
        root = storeRoot("Detalle del producto")
        root.addView(action("Volver al catálogo") { finish() }.apply { isEnabled = !busy })
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(content) },LinearLayout.LayoutParams(-1,0,1f))
        val image = ImageView(this).apply { contentDescription = p.title; scaleType = ImageView.ScaleType.FIT_CENTER }
        content.addView(image,LinearLayout.LayoutParams(-1,dp(240)))
        Glide.with(this).load(p.image).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_menu_report_image).into(image)
        content.addView(label(p.category,14f)); content.addView(label(p.title,23f))
        content.addView(label(String.format(Locale.US,"$%.2f",p.price),28f))
        p.rating?.let { content.addView(label("${it.rate} / 5 · ${it.count} valoraciones")) }
        content.addView(label(p.description,17f))
        val session = SessionManager.getSession(this)
        if (session?.role == UserRole.CLIENTE) content.addView(action("Agregar al carrito (${CartState.count()})") { CartState.add(p.id); render() })
        // Nunca se construyen botones de gestión para Cliente o Auditor.
        if (session?.role == UserRole.ADMINISTRADOR) {
            content.addView(label("Edición y eliminación de demostración: la API no guarda los cambios.",14f))
            content.addView(action("Editar") { edit() }.apply { isEnabled = !busy })
            content.addView(action("Eliminar") { confirmDelete() }.apply { isEnabled = !busy })
            if(busy) content.addView(ProgressBar(this))
        }
    }
    private fun confirmDelete() {
        if(busy) return
        AlertDialog.Builder(this).setTitle("Eliminar producto")
            .setMessage("La API simula esta operación y conserva el producto. ¿Continuar?")
            .setNegativeButton("Cancelar",null).setPositiveButton("Eliminar") { _,_ ->
                busy = true; render()
                lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) { repository.delete(product!!.id) }
                        Toast.makeText(this@ProductDetailActivity,"Eliminación simulada confirmada.",Toast.LENGTH_LONG).show()
                        setResult(RESULT_OK); finish()
                    } catch(e: CancellationException) { throw e }
                    catch(e: Exception) { Toast.makeText(this@ProductDetailActivity,e.message ?: "No se pudo eliminar.",Toast.LENGTH_LONG).show() }
                    finally { busy = false; if(!isFinishing) render() }
                }
            }.show()
    }
    private fun edit() {
        if(busy) return
        busy = true; render()
        lifecycleScope.launch {
            try {
                val categories = withContext(Dispatchers.IO) { repository.categories() }
                if(categories.isEmpty()) throw ApiException("No hay categorías disponibles. Reintenta.")
                showEditor(categories)
            } catch(e: CancellationException) { throw e }
            catch(e: Exception) { Toast.makeText(this@ProductDetailActivity,e.message ?: "No se pudo abrir el editor.",Toast.LENGTH_LONG).show() }
            finally { busy = false; render() }
        }
    }
    private fun showEditor(categories: List<String>) {
        val p = product ?: return
        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20),dp(12),dp(20),dp(12)) }
        fun field(label: String, value: String, type: Int = InputType.TYPE_CLASS_TEXT): EditText {
            form.addView(this.label(label))
            return EditText(this).apply { setText(value); inputType = type; contentDescription = label; form.addView(this) }
        }
        form.addView(label("La API confirma cambios simulados; no los conserva.",14f))
        val title = field("Título",p.title)
        val price = field("Precio",String.format(Locale.US,"%.2f",p.price),InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        val description = field("Descripción",p.description,InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        val image = field("Imagen (URL HTTPS)",p.image,InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI)
        form.addView(label("Categoría"))
        val category = Spinner(this).apply {
            contentDescription = "Categoría"
            adapter = ArrayAdapter(this@ProductDetailActivity,android.R.layout.simple_spinner_dropdown_item,listOf("Selecciona una categoría") + categories)
            setSelection(categories.indexOf(p.category).let { if(it < 0) 0 else it+1 }); form.addView(this)
        }
        val error = label(""); form.addView(error)
        val dialog = AlertDialog.Builder(this).setTitle("Editar producto").setView(ScrollView(this).apply { addView(form) })
            .setNegativeButton("Cancelar",null).setPositiveButton("Guardar",null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                title.error = ProductRules.text(title.text.toString())
                price.error = ProductRules.price(price.text.toString())
                description.error = ProductRules.text(description.text.toString(),5000)
                image.error = ProductRules.image(image.text.toString())
                error.text = if(category.selectedItemPosition == 0) "Selecciona una categoría." else ""
                if(listOf(title,price,description,image).any { it.error != null } || category.selectedItemPosition == 0) return@setOnClickListener
                val updated = Product(p.id,title.text.toString().trim(),ProductRules.parsePrice(price.text.toString())!!,
                    description.text.toString().trim(),categories[category.selectedItemPosition-1],image.text.toString().trim())
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                dialog.setCancelable(false)
                listOf<View>(title,price,description,image,category).forEach { it.isEnabled = false }
                error.text = "Guardando…"
                lifecycleScope.launch {
                    try {
                        product = withContext(Dispatchers.IO) { repository.update(updated,categories) }
                        Toast.makeText(this@ProductDetailActivity,"Edición simulada confirmada. La API no guarda los cambios.",Toast.LENGTH_LONG).show()
                        dialog.dismiss(); render()
                    } catch(e: CancellationException) { dialog.dismiss(); throw e }
                    catch(e: Exception) { error.text = e.message ?: "No se pudo guardar." }
                    finally {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
                        dialog.setCancelable(true)
                        listOf<View>(title,price,description,image,category).forEach { it.isEnabled = true }
                    }
                }
            }
        }
        dialog.show()
    }
}
