package com.example.fakestoreroles

import org.json.JSONObject

/** Modelo inmutable. El JSON se valida antes de pasar a las vistas. */
data class Product(
    val id: Int, val title: String, val price: Double,
    val description: String, val category: String, val image: String,
    val rating: Rating? = null
) {
    fun toJson() = JSONObject().put("id", id).put("title", title).put("price", price)
        .put("description", description).put("category", category).put("image", image)

    companion object {
        fun fromJson(raw: JSONObject): Product {
            fun required(key: String): String = (raw.opt(key) as? String)?.trim()
                ?.takeIf { it.isNotEmpty() } ?: throw ApiException("Datos de producto inválidos.")
            val id = raw.opt("id") as? Number ?: throw ApiException("ID inválido.")
            val price = raw.opt("price") as? Number ?: throw ApiException("Precio inválido.")
            if (id.toDouble() != id.toInt().toDouble() || id.toInt() <= 0 ||
                !price.toDouble().isFinite() || price.toDouble() < 0) throw ApiException("Datos de producto inválidos.")
            val image = (raw.opt("image") as? String).orEmpty()
            val rating = raw.optJSONObject("rating")?.let {
                val rate = (it.opt("rate") as? Number)?.toDouble()
                val count = (it.opt("count") as? Number)?.toDouble()
                if (rate != null && rate.isFinite() && rate in 0.0..5.0 && count != null &&
                    count >= 0 && count <= Int.MAX_VALUE && count == count.toInt().toDouble()) Rating(rate, count.toInt()) else null
            }
            return Product(id.toInt(), required("title"), price.toDouble(), required("description"),
                required("category"), if (ProductRules.image(image) == null) image.trim() else "", rating)
        }
    }
}
/** Representa la puntuación opcional de un producto. */
data class Rating(val rate: Double, val count: Int)
