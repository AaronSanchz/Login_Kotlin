package com.example.fakestoreroles

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.io.IOException

/** Contrato independiente de Activity, sustituible por un repositorio falso en pruebas. */
interface ProductRepository {
    fun products(category: String? = null): List<Product>
    fun categories(): List<String>
    fun detail(id: Int): Product
    fun update(product: Product, categories: List<String>): Product
    fun delete(id: Int)
}
fun interface StoreTransport {
    fun request(method: String, path: String, body: JSONObject?): String
}

/** Se ejecuta en Dispatchers.IO; siempre libera la conexión, incluso ante un error. */
class HttpStoreTransport : StoreTransport {
    override fun request(method: String, path: String, body: JSONObject?): String {
        val connection = URL("https://fakestoreapi.com$path").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            if (code == 404) throw ApiException("Producto no disponible")
            if (code !in 200..299) throw ApiException("No se pudo completar la consulta. Reintenta.")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                .takeIf { it.isNotBlank() } ?: throw ApiException("La API devolvió una respuesta vacía.")
        } catch (_: SocketTimeoutException) { throw ApiException("La conexión tardó demasiado. Reintenta.") }
        catch (_: IOException) { throw ApiException("Sin conexión. Revisa tu Internet y reintenta.") }
        finally { connection.disconnect() }
    }
}

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
