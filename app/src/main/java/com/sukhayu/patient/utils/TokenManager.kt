package com.sukhayu.patient.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "auth"              // MUST MATCH LoginActivity
    private const val KEY_AUTH_TOKEN = "token"        // MUST MATCH LoginActivity
    private const val KEY_USER_ID = "user_id"
    private const val KEY_SUPREME_ID = "supreme_id"
    private const val KEY_ROLE = "role"

    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String, userId: String = "", supremeId: String = "", role: String = "") {
        sharedPreferences.edit().apply {
            putString(KEY_AUTH_TOKEN, token)
            if (userId.isNotEmpty()) putString(KEY_USER_ID, userId)
            if (supremeId.isNotEmpty()) putString(KEY_SUPREME_ID, supremeId)
            if (role.isNotEmpty()) putString(KEY_ROLE, role)
            apply()
        }
    }

    fun getToken(): String = sharedPreferences.getString(KEY_AUTH_TOKEN, "") ?: ""

    fun getUserId(): String = sharedPreferences.getString(KEY_USER_ID, "") ?: ""

    fun getSupremeId(): String = sharedPreferences.getString(KEY_SUPREME_ID, "") ?: ""

    fun getRole(): String = sharedPreferences.getString(KEY_ROLE, "") ?: ""

    fun isLoggedIn(): Boolean = getToken().isNotEmpty()

    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }
}
