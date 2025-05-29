package ru.locationwatch.mobile_client.network

import android.content.Context

class FCMTokenManager(context: Context) {

    private val prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)

    fun saveToken(fcmToken: String) {
        prefs.edit().apply {
            putString("fcm_token", fcmToken)
            putBoolean("is_token_sent", false)
            apply()
        }
    }

    fun getFcmToken(): String? = prefs.getString("fcm_token", null)

    fun isTokenSent(): Boolean = prefs.getBoolean("is_token_sent", false)

    fun markTokenAsSent() {
        prefs.edit().putBoolean("is_token_sent", true).apply()
    }

    fun clearToken() {
        prefs.edit().remove("fcm_token").remove("is_token_sent").apply()
    }

    fun getTokenAndStatus(): Pair<String?, Boolean> {
        return Pair(
            prefs.getString("fcm_token", null),
            prefs.getBoolean("is_token_sent", false)
        )
    }

}