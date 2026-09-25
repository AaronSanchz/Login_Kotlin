package com.example.fakestoreroles

import java.net.URI

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
