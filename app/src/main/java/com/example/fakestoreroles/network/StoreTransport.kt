// GUÍA DEL ARCHIVO: Transporte HTTP síncrono que debe invocarse en Dispatchers.IO. Envía método, ruta y JSONObject opcional; devuelve texto JSON. Usa tiempos de espera de 15 segundos y libera HttpURLConnection en finally.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.io.IOException

/** Capa de transporte: conexión HTTP, tiempos de espera y traducción de códigos de error. */
fun interface StoreTransport {
    /** Contrato de transporte: recibe método, ruta y JSON opcional; devuelve texto del servidor o lanza error. La implementación real se llama fuera del hilo principal. */
    fun request(method: String, path: String, body: JSONObject?): String
}

/** Se ejecuta en Dispatchers.IO; siempre libera la conexión, incluso ante un error. */
class HttpStoreTransport : StoreTransport {
    /** Contrato de transporte: recibe método, ruta y JSON opcional; devuelve texto del servidor o lanza error. La implementación real se llama fuera del hilo principal. */
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
            if (code !in 200..299) throw ApiException(HttpErrorMapper.message(code, path == "/auth/login"))
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                .takeIf { it.isNotBlank() } ?: throw ApiException("La API devolvió una respuesta vacía.")
        } catch (_: SocketTimeoutException) { throw ApiException("La conexión tardó demasiado. Reintenta.") }
        catch (_: IOException) { throw ApiException("Sin conexión. Revisa tu Internet y reintenta.") }
        finally { connection.disconnect() }
    }
}
