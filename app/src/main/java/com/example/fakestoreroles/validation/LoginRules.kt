// GUÍA DEL ARCHIVO: Reglas locales de credenciales: usuario obligatorio hasta 100 caracteres; contraseña obligatoria hasta 256. Devuelven mensaje o null. LoginController las aplica antes del transporte.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Valida credenciales antes de enviar la solicitud de acceso. */
object LoginRules {
    /** Valida nombre de usuario obligatorio con límite de 100 caracteres; devuelve mensaje o null. */
    fun username(value: String): String? = when {
        value.isBlank() -> "Escribe tu usuario."
        value.length > 100 -> "El usuario admite hasta 100 caracteres."
        else -> null
    }
    /** Valida contraseña obligatoria con límite de 256 caracteres; devuelve mensaje o null. */
    fun password(value: String): String? = when {
        value.isBlank() -> "Escribe tu contraseña."
        value.length > 256 -> "La contraseña admite hasta 256 caracteres."
        else -> null
    }
}
