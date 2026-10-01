// GUÍA DEL ARCHIVO: Estado global temporal del carrito de Cliente; conserva IDs, no pagos ni inventario. Se reinicia al cerrar sesión para evitar que otro usuario vea elementos anteriores.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Estado temporal del carrito y conteo de productos. */

/** Cuenta los productos agregados al carrito durante la sesión. */
object CartState {
    private val productIds = mutableListOf<Int>()

    /** Añade un ID positivo al carrito temporal; los repetidos cuentan como elementos separados. */
    fun add(productId: Int) {
        if (productId > 0) productIds.add(productId)
    }

    /** Devuelve el número actual de elementos del carrito, contando IDs repetidos. */
    fun count(): Int = productIds.size

    /** Vacía todos los elementos del carrito global; se llama al finalizar una sesión. */
    fun clear() {
        productIds.clear()
    }
}
