package com.example.zerowaste.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val USER_ID = "user_id"
    }

    /**
     * Saves the user's authentication token and ID.
     */
    fun saveSession(token: String, userId: Long) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.putLong(USER_ID, userId)
        editor.apply()
    }

    /**
     * Retrieves the authentication token.
     */
    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    /**
     * Retrieves the logged-in user's ID.
     */
    fun getUserId(): Long? {
        // Return null if the user ID isn't found (returns 0 by default if not found)
        val id = prefs.getLong(USER_ID, -1L)
        return if (id == -1L) null else id
    }

    /**
     * Clears all session data (token and user ID).
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.remove(USER_ID)
        editor.apply()
    }
}
