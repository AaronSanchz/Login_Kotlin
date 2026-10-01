// GUÍA DEL ARCHIVO: Sesión cifrada con EncryptedSharedPreferences y MasterKey. Guarda token, ID, nombre y rol; commit comprueba escritura o borrado. getSession devuelve null si los datos no permiten recuperar una sesión.
// Consulta docs/GUIA_APRENDIZAJE_US01_US08.html para sintaxis, recorridos y ejercicios.

package com.example.fakestoreroles

/** Almacenamiento seguro de la sesión. */

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Guarda y recupera la sesión local cifrada. */
object SessionManager {
    private const val FILE_NAME = "secure_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROLE = "role"

    /** Abre las preferencias cifradas con clave del sistema; centraliza algoritmos y archivo de almacenamiento. */
    private fun preferences(context: Context) = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /** Persiste token, ID, nombre y rol de la sesión; nunca almacena contraseña. Solo se navega después del guardado. */
    fun saveSession(context: Context, session: SessionData) {
        val saved = preferences(context).edit()
            .putString(KEY_TOKEN, session.token)
            .putInt(KEY_USER_ID, session.userId)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_ROLE, session.role.name)
            .commit()
        if (!saved) throw ApiException("No se pudo guardar la sesión en el dispositivo.")
    }

    /** Recupera sesión cifrada o null ante fallo; no concede permisos si no puede leer los datos. */
    fun getSession(context: Context): SessionData? = try { readSession(context) } catch (_: Exception) { null }

    /** Valida campos de preferencias cifradas y convierte el nombre del rol a UserRole. */
    private fun readSession(context: Context): SessionData? {
        val prefs = preferences(context)
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val roleText = prefs.getString(KEY_ROLE, null) ?: return null
        val userId = prefs.getInt(KEY_USER_ID, -1)

        if (token.isBlank() || username.isBlank() || userId < 1) return null

        val role = try {
            UserRole.valueOf(roleText)
        } catch (_: Exception) {
            clearSession(context)
            return null
        }

        return SessionData(
            token = token,
            userId = userId,
            username = username,
            role = role
        )
    }

    /** Elimina los datos persistidos de sesión; el llamador debe limpiar además el carrito y el historial visual. */
    fun clearSession(context: Context) {
        if (!preferences(context).edit().clear().commit()) {
            throw ApiException("No se pudo borrar la sesión del dispositivo.")
        }
    }
}
