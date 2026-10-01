// GUÍA DEL ARCHIVO: ProductRepository desacopla vistas de HTTP. HttpProductRepository valida datos JSON y bloquea escrituras sin Administrador antes de StoreTransport.request. create quita ID; update/delete verifican el ID confirmado.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Contrato y servicio HTTP de productos con validación y permisos. */

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/** Contrato independiente de Activity, sustituible por un repositorio falso en pruebas. */
interface ProductRepository {
    /** Obtiene lista general o categoría recibida; valida y convierte cada JSON en Product. Una categoría vacía o respuesta incoherente es error. */
    fun products(category: String? = null): List<Product>
    /** Solicita /products/categories y devuelve nombres no vacíos y sin duplicados; no depende de la lista de productos. */
    fun categories(): List<String>
    /** Consulta /products/{id}; rechaza ID no positivo y confirma que la respuesta corresponde al artículo solicitado. */
    fun detail(id: Int): Product
    /** Revalida Administrador y campos; envía POST /products sin ID. Devuelve el producto con ID del servidor; Fake Store no lo persiste. */
    fun create(product: Product, categories: List<String>): Product
    /** Revalida Administrador, campos y categoría; envía PUT /products/{id} y devuelve la confirmación del mismo ID. */
    fun update(product: Product, categories: List<String>): Product
    /** Revalida Administrador e ID; envía DELETE /products/{id} y exige un ID coincidente. La confirmación visual pertenece a la pantalla llamadora. */
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

    /** Obtiene lista general o categoría recibida; valida y convierte cada JSON en Product. Una categoría vacía o respuesta incoherente es error. */
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
    /** Solicita /products/categories y devuelve nombres no vacíos y sin duplicados; no depende de la lista de productos. */
    override fun categories(): List<String> = parse {
        val array = JSONArray(transport.request("GET", "/products/categories", null))
        List(array.length()) {
            (array.opt(it) as? String)?.trim()?.takeIf { value -> value.isNotEmpty() }
                ?: throw ApiException("Categorías inválidas.")
        }.distinct()
    }
    /** Consulta /products/{id}; rechaza ID no positivo y confirma que la respuesta corresponde al artículo solicitado. */
    override fun detail(id: Int): Product {
        if (id <= 0) throw ApiException("Producto no disponible")
        return parse { Product.fromJson(JSONObject(transport.request("GET", "/products/$id", null))) }
            .also { if (it.id != id) throw ApiException("Producto no disponible") }
    }
    /** Comprueba sesión actual en el proveedor inyectado; impide POST, PUT y DELETE a Cliente, Auditor o sesión ausente. */
    private fun requireAdmin() {
        if (session()?.role != UserRole.ADMINISTRADOR) throw ApiException("No tienes permiso para esta acción.")
    }
    /** US06: aplica permiso y validación antes de emitir POST. */
    /** Revalida Administrador y campos; envía POST /products sin ID. Devuelve el producto con ID del servidor; Fake Store no lo persiste. */
    override fun create(product: Product, categories: List<String>): Product {
        requireAdmin()
        ProductRules.validateNew(product, categories)
        val body = product.toJson().apply { remove("id") }
        return parse { Product.fromJson(JSONObject(transport.request("POST", "/products", body))) }
    }
    /** Revalida Administrador, campos y categoría; envía PUT /products/{id} y devuelve la confirmación del mismo ID. */
    override fun update(product: Product, categories: List<String>): Product {
        requireAdmin()
        ProductRules.validate(product, categories)
        return parse { Product.fromJson(JSONObject(transport.request("PUT", "/products/${product.id}", product.toJson()))) }
            .also { if (it.id != product.id) throw ApiException("La edición no fue confirmada.") }
    }
    /** Revalida Administrador e ID; envía DELETE /products/{id} y exige un ID coincidente. La confirmación visual pertenece a la pantalla llamadora. */
    override fun delete(id: Int) {
        requireAdmin()
        if (id <= 0) throw ApiException("Producto no disponible")
        parse {
            val result = JSONObject(transport.request("DELETE", "/products/$id", null))
            if (result.opt("id") != id) throw ApiException("La eliminación no fue confirmada.")
        }
    }
}
