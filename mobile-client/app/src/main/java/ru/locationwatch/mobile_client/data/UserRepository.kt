package ru.locationwatch.mobile_client.data

import retrofit2.HttpException
import ru.locationwatch.mobile_client.network.UserService
import ru.locationwatch.mobile_client.network.models.UserResponse

interface UserRepository {

    suspend fun getUser(id: Int): UserResponse

}

class NetworkUserRepository(
    private val userService: UserService
) : UserRepository {

    override suspend fun getUser(id: Int): UserResponse {
        val response = userService.getUser(id)
        return response.body() ?: throw HttpException(response)
    }

}