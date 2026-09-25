package com.example.fakestoreroles

/** Convierte códigos HTTP en mensajes claros; conserva el código para diagnóstico. */
object HttpErrorMapper {
    fun message(code: Int, login: Boolean = false): String = when (code) {
        400, 401 -> if (login) "Usuario o contraseña inválidos (HTTP $code)." else "Solicitud o acceso no válido (HTTP $code)."
        403 -> "Acceso rechazado por el servicio (HTTP 403)."
        404 -> "Producto no disponible (HTTP 404)."
        408, 504 -> "El servicio tardó demasiado (HTTP $code). Reintenta."
        429 -> "Demasiadas solicitudes (HTTP 429). Espera y reintenta."
        in 500..599 -> "El servicio no está disponible (HTTP $code). Reintenta."
        else -> "No se pudo completar la consulta (HTTP $code)."
    }
}
