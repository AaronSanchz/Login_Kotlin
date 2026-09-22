package com.example.fakestoreroles

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SessionManager {
    private const val FILE_NAME = "secure_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROLE = "role"

    private fun preferences(context: Context) = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveSession(context: Context, session: SessionData) {
        preferences(context).edit()
            .putString(KEY_TOKEN, session.token)
            .putInt(KEY_USER_ID, session.userId)
            .putString(KEY_USERNAME, session.username)
            .putString(KEY_ROLE, session.role.name)
            .apply()
    }

    fun getSession(context: Context): SessionData? = try { readSession(context) } catch (_: Exception) { null }

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

    fun clearSession(context: Context) {
        preferences(context).edit().clear().apply()
    }
}
