package ru.locationwatch.mobile_client.data

import android.util.Log
import retrofit2.Response
import ru.locationwatch.mobile_client.network.models.AuthRequest
import ru.locationwatch.mobile_client.network.models.JWTResponse
import ru.locationwatch.mobile_client.network.AuthService
import ru.locationwatch.mobile_client.network.TokenManager

interface AuthRepository {

    suspend fun login(username: String, password: String) : JWTResponse

    suspend fun register(username: String, password: String)
}

class NetworkAuthRepository(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(username: String, password: String) : JWTResponse {
        val response = authService.login(AuthRequest(username, password))
        return response.body() ?: throw Exception("Login failed: ${response.code()}")
    }

    override suspend fun register(username: String, password: String) {
        val response = authService.register(AuthRequest(username, password))
        if (!response.isSuccessful) {
            throw Exception("Registration failed: ${response.code()}")
        }
    }

}