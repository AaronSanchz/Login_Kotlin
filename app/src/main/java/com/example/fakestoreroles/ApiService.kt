package com.example.fakestoreroles

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

enum class UserRole(val label: String) {
    ADMINISTRADOR("Administrador"),
    AUDITOR("Auditor"),
    CLIENTE("Cliente")
}

data class SessionData(
    val token: String,
    val userId: Int,
    val username: String,
    val role: UserRole
)

class ApiException(message: String) : Exception(message)

object ApiService {
    private const val BASE_URL = "https://fakestoreapi.com"

    fun roleFromId(id: Int): UserRole {
        return when (id) {
            1, 2 -> UserRole.ADMINISTRADOR
            3 -> UserRole.AUDITOR
            else -> UserRole.CLIENTE
        }
    }

    fun authenticate(username: String, password: String): SessionData {
        if (username.isBlank() || password.isBlank() || username.length > 100 || password.length > 256) throw ApiException("Revisa usuario y contraseña.")
        val token = loginToken(username, password)
        val users = getUsers()
        val user = users.firstOrNull { it.username.equals(username, ignoreCase = true) }
            ?: throw ApiException("No se pudo obtener la información del usuario.")

        if (user.id <= 0 || user.username.isBlank()) throw ApiException("Datos de usuario inválidos.")
        return SessionData(
            token = token,
            userId = user.id,
            username = user.username,
            role = roleFromId(user.id)
        )
    }

    private fun loginToken(username: String, password: String): String {
        val connection = URL("$BASE_URL/auth/login").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            val body = JSONObject()
                .put("username", username)
                .put("password", password)
                .toString()

            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode
            val response = readResponse(connection, code)

            if (code in 200..299) {
                val token = (JSONObject(response).opt("token") as? String)?.trim().orEmpty()
                if (token.isBlank()) {
                    throw ApiException("La API no devolvió un token válido.")
                }
                token
            } else if (code == 400 || code == 401) {
                throw ApiException("Usuario o contraseña inválidos.")
            } else {
                throw ApiException("No se pudo iniciar sesión. Error $code.")
            }
        } finally {
            connection.disconnect()
        }
    }

    fun getUsers(): List<UserItem> {
        val response = get("/users")
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

    private fun get(path: String): String {
        val connection = URL("$BASE_URL$path").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")

            val code = connection.responseCode
            val response = readResponse(connection, code)
            if (code !in 200..299) {
                throw IOException("Error HTTP $code")
            }
            response
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection, code: Int): String {
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }
}

data class UserItem(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val phone: String
)

