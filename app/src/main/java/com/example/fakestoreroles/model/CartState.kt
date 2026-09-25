package com.example.fakestoreroles

/** Estado temporal del carrito y conteo de productos. */

/** Cuenta los productos agregados al carrito durante la sesión. */
object CartState {
    private val productIds = mutableListOf<Int>()

    fun add(productId: Int) {
        if (productId > 0) productIds.add(productId)
    }

    fun count(): Int = productIds.size

    fun clear() {
        productIds.clear()
    }
}
