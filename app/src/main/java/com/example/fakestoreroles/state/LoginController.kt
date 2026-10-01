// GUÍA DEL ARCHIVO: Valida usuario/contraseña y delega a ApiService con transporte sustituible. authenticate devuelve SessionData o lanza ApiException; la Activity se encarga de conexión, almacenamiento y navegación.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Coordina la validación y el servicio de acceso; la vista solo presenta resultados. */
class LoginController(private val transport: StoreTransport = HttpStoreTransport()) {
    /** Rechaza credenciales inválidas antes de iniciar cualquier conexión. */
    /** Valida credenciales y obtiene token/usuario para construir SessionData; no elude errores de autenticación. */
    fun authenticate(username: String, password: String): SessionData {
        val user = username.trim()
        LoginRules.username(user)?.let { throw ApiException(it) }
        LoginRules.password(password)?.let { throw ApiException(it) }
        return ApiService.authenticate(user, password, transport)
    }
}
