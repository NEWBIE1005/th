package com.example.project2.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.project2.entities.Account

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveSession(account: Account) {
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putLong("ACCOUNT_ID", account.id)
            putString("USERNAME", account.username)
            putString("ROLE", account.role)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("IS_LOGGED_IN", false)

    fun getRole(): String = prefs.getString("ROLE", "") ?: ""

    fun getAccountId(): Long = prefs.getLong("ACCOUNT_ID", -1L)

    fun getUsername(): String = prefs.getString("USERNAME", "") ?: ""

    fun logout() {
        prefs.edit().clear().apply()
    }
}
