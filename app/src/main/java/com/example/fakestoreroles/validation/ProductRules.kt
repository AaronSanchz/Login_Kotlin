// GUÍA DEL ARCHIVO: Reglas locales usadas en EditText y repositorio: texto, precio positivo hasta un millón con dos decimales, HTTPS y categoría. validateNew reutiliza validate con ID auxiliar; ese ID no se manda en POST.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

import java.net.URI

/** Reglas reutilizadas en formulario y repositorio, también verificables con JUnit. */
object ProductRules {
    /** Valida texto obligatorio y longitud; null significa válido y un mensaje significa error para el campo. */
    fun text(value: String, max: Int = 200): String? = when {
        value.isBlank() -> "Este campo es obligatorio."
        value.trim().length > max -> "Máximo $max caracteres."
        else -> null
    }
    /** Normaliza coma a punto e intenta convertir a número; no reemplaza la validación completa de price. */
    fun parsePrice(value: String): Double? = value.trim().replace(',', '.').toDoubleOrNull()
    /** Valida formato numérico local, positividad, límite de un millón y hasta dos decimales; devuelve mensaje o null. */
    fun price(value: String): String? {
        val number = parsePrice(value)
        return if (!Regex("^\\d+([.,]\\d{1,2})?$").matches(value.trim()) || number == null ||
            !number.isFinite() || number <= 0 || number > 1000000) "Usa un precio mayor que 0, hasta 1000000 y con hasta 2 decimales." else null
    }
    /** Exige URL HTTPS con host y sin credenciales incrustadas; valida formato, no descarga la imagen. */
    fun image(value: String): String? = try {
        val uri = URI(value.trim())
        if (uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null) null
        else "Introduce una URL HTTPS válida."
    } catch (_: Exception) { "Introduce una URL HTTPS válida." }
    /** Valida producto para edición con ID positivo, texto, precio, imagen y categoría; falla antes de HTTP. */
    fun validate(p: Product, categories: List<String>) {
        if (p.id <= 0 || text(p.title) != null || text(p.description, 5000) != null ||
            !p.price.isFinite() || p.price <= 0 || p.price > 1000000 || price(p.price.toString()) != null || image(p.image) != null ||
            p.category !in categories) throw ApiException("Revisa los campos del producto.")
    }
    /** El alta comparte reglas con la edición; el servidor asigna el ID. */
    /** Reutiliza reglas de edición con ID auxiliar solo para validar; el servidor asigna el ID de creación. */
    fun validateNew(p: Product, categories: List<String>) = validate(p.copy(id = 1), categories)
}
