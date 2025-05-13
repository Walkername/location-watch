package ru.locationwatch.mobile_client.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import ru.locationwatch.mobile_client.network.models.UserResponse

interface UserService {

    @GET("/users/{id}")
    suspend fun getUser(
        @Path("id") id: Int
    ): Response<UserResponse>

}