package ru.locationwatch.mobile_client.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.locationwatch.mobile_client.network.models.AuthRequest
import ru.locationwatch.mobile_client.network.models.JWTResponse

interface AuthService {

    @POST("/auth/login")
    suspend fun login(
        @Body authRequest: AuthRequest
    ): Response<JWTResponse>

    @POST("/auth/register")
    suspend fun register(
        @Body authRequest: AuthRequest
    ): Response<Unit>

}