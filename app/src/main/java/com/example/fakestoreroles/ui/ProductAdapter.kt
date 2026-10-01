// GUÍA DEL ARCHIVO: ListAdapter recicla filas de RecyclerView. Holder conserva vistas; onBindViewHolder asigna datos y Glide descarga imagen. Diff compara ID y contenido; al reciclar se libera la carga de imagen.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Filas seleccionables de productos del catálogo. */

import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.Locale

/** Recicla filas e imágenes: nunca coloca todo el catálogo en un ScrollView. */
class ProductAdapter(private val select: (Product) -> Unit) : ListAdapter<Product, ProductAdapter.Holder>(Diff) {
    class Holder(val row: LinearLayout, val image: ImageView, val title: TextView, val price: TextView, val category: TextView) : RecyclerView.ViewHolder(row)
    /** Construye una fila y sus vistas una vez para que RecyclerView pueda reutilizarlas. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val c = parent.context
        val row = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL; setPadding(c.dp(16), c.dp(16), c.dp(16), c.dp(16)); background = c.cardBackground()
            layoutParams = RecyclerView.LayoutParams(-1, -2).apply { bottomMargin = c.dp(12) }
            isFocusable = true; isClickable = true
        }
        val image = ImageView(c).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
        row.addView(image, LinearLayout.LayoutParams(c.dp(88), c.dp(120)))
        val column = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setPadding(c.dp(16), 0, 0, 0) }
        row.addView(column, LinearLayout.LayoutParams(0, -2, 1f))
        val category = c.label("", 12f); val title = c.label("", 17f); val price = c.label("", 22f)
        column.addView(category); column.addView(title); column.addView(price); column.addView(c.label("Ver detalle", 13f))
        return Holder(row, image, title, price, category)
    }
    /** Asigna Product a una fila reciclada, carga imagen con Glide y conecta pulsación a select(product). */
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val p = getItem(position)
        holder.title.text = p.title; holder.category.text = p.category
        holder.price.text = String.format(Locale.US, "$%.2f", p.price)
        holder.image.contentDescription = p.title
        Glide.with(holder.image).load(p.image).placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image).fitCenter().into(holder.image)
        holder.row.setOnClickListener { select(p) }
    }
    /** Cancela y limpia la imagen del Holder antes de devolverlo al reciclador. */
    override fun onViewRecycled(holder: Holder) { Glide.with(holder.image).clear(holder.image); super.onViewRecycled(holder) }
    private object Diff : DiffUtil.ItemCallback<Product>() {
        /** Compara IDs para decidir si dos registros representan el mismo artículo. */
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        /** Compara datos completos para decidir si RecyclerView debe redibujar una fila. */
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
