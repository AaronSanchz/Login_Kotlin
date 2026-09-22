package com.example.fakestoreroles

import org.json.JSONObject
import java.net.URI

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
data class Rating(val rate: Double, val count: Int)

/** Reglas reutilizadas en formulario y repositorio, también verificables con JUnit. */
object ProductRules {
    fun text(value: String, max: Int = 200): String? = when {
        value.isBlank() -> "Este campo es obligatorio."
        value.trim().length > max -> "Máximo $max caracteres."
        else -> null
    }
    fun parsePrice(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()
    fun price(value: String): String? {
        val number = parsePrice(value)
        return if (!Regex("^\\d+([.,]\\d{1,2})?$").matches(value.trim()) || number == null ||
            !number.isFinite() || number <= 0 || number > 1000000) "Usa un precio mayor que 0, hasta 1000000 y con hasta 2 decimales." else null
    }
    fun image(value: String): String? = try {
        val uri = URI(value.trim())
        if (uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null) null
        else "Introduce una URL HTTPS válida."
    } catch (_: Exception) { "Introduce una URL HTTPS válida." }
    fun validate(p: Product, categories: List<String>) {
        if (p.id <= 0 || text(p.title) != null || text(p.description, 5000) != null ||
            !p.price.isFinite() || p.price <= 0 || p.price > 1000000 || image(p.image) != null ||
            p.category !in categories) throw ApiException("Revisa los campos del producto.")
    }
}
