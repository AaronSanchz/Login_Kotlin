package com.example.fakestoreroles

object CartState {
    private val productIds = mutableListOf<Int>()

    fun add(productId: Int) {
        productIds.add(productId)
    }

    fun count(): Int = productIds.size

    fun clear() {
        productIds.clear()
    }
}
