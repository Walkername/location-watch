package ru.locationwatch.mobile_client.data

import retrofit2.HttpException
import ru.locationwatch.mobile_client.network.models.AuthRequest
import ru.locationwatch.mobile_client.network.models.JWTResponse
import ru.locationwatch.mobile_client.network.AuthService
import ru.locationwatch.mobile_client.network.models.RefreshTokenRequest

interface AuthRepository {

    suspend fun login(username: String, password: String) : JWTResponse

    suspend fun register(username: String, password: String)

    suspend fun refresh(refreshToken: String) : JWTResponse
}

class NetworkAuthRepository(
    private val authService: AuthService
) : AuthRepository {

    override suspend fun login(username: String, password: String) : JWTResponse {
        val response = authService.login(AuthRequest(username, password))
        return response.body() ?: throw HttpException(response)
    }

    override suspend fun register(username: String, password: String) {
        val response = authService.register(AuthRequest(username, password))
        return response.body() ?: throw HttpException(response)
    }

    override suspend fun refresh(refreshToken: String): JWTResponse {
        val response = authService.refresh(RefreshTokenRequest(refreshToken))
        return response.body() ?: throw  HttpException(response)
    }

}