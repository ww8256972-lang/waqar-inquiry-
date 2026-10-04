package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserRole
import java.security.MessageDigest
import java.util.UUID

object SecurityUtils {

    fun hashPassword(password: String, salt: String = "waqar_secure_salt_2026"): String {
        val input = salt + password + "waqar_app_integrity"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String, salt: String = "waqar_secure_salt_2026"): Boolean {
        return hashPassword(password, salt) == hash
    }
}

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("waqar_secure_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLE = "user_role"
        private const val KEY_MUST_CHANGE_PWD = "must_change_pwd"
        private const val KEY_SESSION_TOKEN = "session_token"
    }

    fun saveSession(userId: String, username: String, fullName: String, role: String, mustChangePwd: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_ROLE, role)
            .putBoolean(KEY_MUST_CHANGE_PWD, mustChangePwd)
            .putString(KEY_SESSION_TOKEN, UUID.randomUUID().toString())
            .apply()
    }

    fun updatePasswordChanged() {
        prefs.edit().putBoolean(KEY_MUST_CHANGE_PWD, false).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "Guest") ?: "Guest"
    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "User") ?: "User"
    fun getRole(): UserRole {
        val roleStr = prefs.getString(KEY_ROLE, UserRole.STAFF.name) ?: UserRole.STAFF.name
        return try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.STAFF
        }
    }
    fun mustChangePassword(): Boolean = prefs.getBoolean(KEY_MUST_CHANGE_PWD, false)

    fun hasPermission(requiredRole: UserRole): Boolean {
        val current = getRole()
        return when (requiredRole) {
            UserRole.SUPER_ADMIN -> current == UserRole.SUPER_ADMIN
            UserRole.ADMIN -> current == UserRole.SUPER_ADMIN || current == UserRole.ADMIN
            UserRole.MANAGER -> current == UserRole.SUPER_ADMIN || current == UserRole.ADMIN || current == UserRole.MANAGER
            UserRole.STAFF -> true
        }
    }
}
