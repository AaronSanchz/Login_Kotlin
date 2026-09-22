package com.example.fakestoreroles

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
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val p = getItem(position)
        holder.title.text = p.title; holder.category.text = p.category
        holder.price.text = String.format(Locale.US, "$%.2f", p.price)
        holder.image.contentDescription = p.title
        Glide.with(holder.image).load(p.image).placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image).fitCenter().into(holder.image)
        holder.row.setOnClickListener { select(p) }
    }
    override fun onViewRecycled(holder: Holder) { Glide.with(holder.image).clear(holder.image); super.onViewRecycled(holder) }
    private object Diff : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
