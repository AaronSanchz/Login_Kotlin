package com.example.fakestoreroles

/** Autenticación, usuarios y asignación local de roles. */

import org.json.JSONArray
import org.json.JSONObject




/** Define los tres roles y sus etiquetas visibles. */
enum class UserRole(val label: String) {
    ADMINISTRADOR("Administrador"),
    AUDITOR("Auditor"),
    CLIENTE("Cliente")
}

/** Agrupa token, usuario y rol de la sesión autenticada. */
data class SessionData(
    val token: String,
    val userId: Int,
    val username: String,
    val role: UserRole
)

/** Describe un error de autenticación o de consulta de usuarios. */
class ApiException(message: String) : Exception(message)

/** Autentica credenciales, consulta usuarios y asigna roles. */
object ApiService {


    fun roleFromId(id: Int): UserRole {
        return when (id) {
            1, 2 -> UserRole.ADMINISTRADOR
            3 -> UserRole.AUDITOR
            else -> UserRole.CLIENTE
        }
    }

    fun authenticate(username: String, password: String, transport: StoreTransport = HttpStoreTransport()): SessionData {
        if (username.isBlank() || password.isBlank() || username.length > 100 || password.length > 256) throw ApiException("Revisa usuario y contraseña.")
        val token = loginToken(username, password, transport)
        val users = getUsers(transport)
        val user = users.firstOrNull { it.username == username }
            ?: throw ApiException("No se pudo obtener la información del usuario.")

        if (user.id <= 0 || user.username.isBlank()) throw ApiException("Datos de usuario inválidos.")
        return SessionData(
            token = token,
            userId = user.id,
            username = user.username,
            role = roleFromId(user.id)
        )
    }

    private fun loginToken(username: String, password: String, transport: StoreTransport): String {
        val body = JSONObject().put("username", username).put("password", password)
        return try {
            val response = transport.request("POST", "/auth/login", body)
            (JSONObject(response).opt("token") as? String)?.trim()?.takeIf { it.isNotEmpty() }
                ?: throw ApiException("La API no devolvió un token válido.")
        } catch (_: org.json.JSONException) { throw ApiException("La API devolvió un login inválido. Reintenta.") }
    }
    fun getUsers(transport: StoreTransport = HttpStoreTransport()): List<UserItem> {
        val response = transport.request("GET", "/users", null)
        val array = JSONArray(response)
        val users = mutableListOf<UserItem>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)
            val name = item.optJSONObject("name")
            users.add(
                UserItem(
                    id = item.optInt("id"),
                    firstName = name?.optString("firstname").orEmpty(),
                    lastName = name?.optString("lastname").orEmpty(),
                    username = item.optString("username"),
                    email = item.optString("email"),
                    phone = item.optString("phone")
                )
            )
        }
        return users
    }

}

/** Representa la respuesta de un usuario de Fake Store API. */
data class UserItem(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val phone: String
)
