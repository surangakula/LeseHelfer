package com.example.lesehelfer.data

import android.content.Context

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("lesehelfer_prefs", Context.MODE_PRIVATE)

    fun getApiKey(fallbackKey: String): String {
        val saved = prefs.getString("openrouter_api_key", null)
        return if (!saved.isNullOrBlank()) saved else fallbackKey
    }

    fun saveApiKey(key: String) {
        prefs.edit().putString("openrouter_api_key", key.trim()).apply()
    }
}
