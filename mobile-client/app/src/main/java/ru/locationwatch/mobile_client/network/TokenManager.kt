package ru.locationwatch.mobile_client.network

import android.content.Context
import android.util.Base64
import com.google.gson.Gson
import java.nio.charset.StandardCharsets

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)
    fun clearTokens() = prefs.edit().clear().apply()

    fun getUserId(): Int? {
        val accessToken = getAccessToken() ?: return null
        return try {
            // Split JWT into parts
            val parts = accessToken.split(".")
            if (parts.size != 3) return null

            // Decode payload
            val payload = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING
            ).toString(StandardCharsets.UTF_8)

            // Parse JSON and extract user ID (adjust claim name if needed)
            val claims = Gson().fromJson(payload, Map::class.java)
            (claims["id"] as? Double)?.toInt()
        } catch (e: Exception) {
            null
        }
    }

    fun getAccessExpirationTime(): Long? {
        val accessToken = getAccessToken() ?: return null
        return try {
            // Split JWT into parts
            val parts = accessToken.split(".")
            if (parts.size != 3) return null

            // Decode payload
            val payload = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING
            ).toString(StandardCharsets.UTF_8)

            // Parse JSON and extract user ID (adjust claim name if needed)
            val claims = Gson().fromJson(payload, Map::class.java)
            (claims["exp"] as? Double)?.toLong()
        } catch (e: Exception) {
            null
        }
    }

    fun getRefreshExpirationTime(): Long? {
        val refreshToken = getRefreshToken() ?: return null
        return try {
            // Split JWT into parts
            val parts = refreshToken.split(".")
            if (parts.size != 3) return null

            // Decode payload
            val payload = Base64.decode(
                parts[1],
                Base64.URL_SAFE or Base64.NO_PADDING
            ).toString(StandardCharsets.UTF_8)

            // Parse JSON and extract user ID (adjust claim name if needed)
            val claims = Gson().fromJson(payload, Map::class.java)
            (claims["exp"] as? Double)?.toLong()
        } catch (e: Exception) {
            null
        }
    }
}