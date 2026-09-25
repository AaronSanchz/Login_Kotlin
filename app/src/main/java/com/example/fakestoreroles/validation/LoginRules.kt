package com.example.fakestoreroles

/** Valida credenciales antes de enviar la solicitud de acceso. */
object LoginRules {
    fun username(value: String): String? = when {
        value.isBlank() -> "Escribe tu usuario."
        value.length > 100 -> "El usuario admite hasta 100 caracteres."
        else -> null
    }
    fun password(value: String): String? = when {
        value.isBlank() -> "Escribe tu contraseña."
        value.length > 256 -> "La contraseña admite hasta 256 caracteres."
        else -> null
    }
}
