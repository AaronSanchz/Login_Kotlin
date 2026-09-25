package com.example.fakestoreroles

/** Coordina la validación y el servicio de acceso; la vista solo presenta resultados. */
class LoginController(private val transport: StoreTransport = HttpStoreTransport()) {
    /** Rechaza credenciales inválidas antes de iniciar cualquier conexión. */
    fun authenticate(username: String, password: String): SessionData {
        val user = username.trim()
        LoginRules.username(user)?.let { throw ApiException(it) }
        LoginRules.password(password)?.let { throw ApiException(it) }
        return ApiService.authenticate(user, password, transport)
    }
}
