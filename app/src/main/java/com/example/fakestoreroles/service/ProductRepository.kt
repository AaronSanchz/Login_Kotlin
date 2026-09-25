package com.example.fakestoreroles

/** Contrato y servicio HTTP de productos con validación y permisos. */

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/** Contrato independiente de Activity, sustituible por un repositorio falso en pruebas. */
interface ProductRepository {
    fun products(category: String? = null): List<Product>
    fun categories(): List<String>
    fun detail(id: Int): Product
    fun update(product: Product, categories: List<String>): Product
    fun delete(id: Int)
}
/** Consulta y modifica productos mediante el contrato del repositorio. */
class HttpProductRepository(
    private val session: () -> SessionData?,
    private val transport: StoreTransport = HttpStoreTransport()
) : ProductRepository {
    private fun <T> parse(block: () -> T): T = try { block() }
        catch (e: ApiException) { throw e }
        catch (_: org.json.JSONException) { throw ApiException("La API devolvió datos inválidos.") }

    override fun products(category: String?): List<Product> {
        if (category != null && category.isBlank()) throw ApiException("Categoría inválida.")
        val path = if (category == null) "/products" else "/products/category/" +
            URLEncoder.encode(category, "UTF-8").replace("+", "%20")
        return parse {
            val array = JSONArray(transport.request("GET", path, null))
            List(array.length()) { Product.fromJson(array.getJSONObject(it)) }.also { list ->
                if (category != null && list.any { it.category != category }) throw ApiException("Categoría de respuesta inválida.")
            }
        }
    }
    override fun categories(): List<String> = parse {
        val array = JSONArray(transport.request("GET", "/products/categories", null))
        List(array.length()) {
            (array.opt(it) as? String)?.trim()?.takeIf { value -> value.isNotEmpty() }
                ?: throw ApiException("Categorías inválidas.")
        }.distinct()
    }
    override fun detail(id: Int): Product {
        if (id <= 0) throw ApiException("Producto no disponible")
        return parse { Product.fromJson(JSONObject(transport.request("GET", "/products/$id", null))) }
            .also { if (it.id != id) throw ApiException("Producto no disponible") }
    }
    private fun requireAdmin() {
        if (session()?.role != UserRole.ADMINISTRADOR) throw ApiException("No tienes permiso para esta acción.")
    }
    override fun update(product: Product, categories: List<String>): Product {
        requireAdmin()
        ProductRules.validate(product, categories)
        return parse { Product.fromJson(JSONObject(transport.request("PUT", "/products/${product.id}", product.toJson()))) }
            .also { if (it.id != product.id) throw ApiException("La edición no fue confirmada.") }
    }
    override fun delete(id: Int) {
        requireAdmin()
        if (id <= 0) throw ApiException("Producto no disponible")
        parse {
            val result = JSONObject(transport.request("DELETE", "/products/$id", null))
            if (result.opt("id") != id) throw ApiException("La eliminación no fue confirmada.")
        }
    }
}
