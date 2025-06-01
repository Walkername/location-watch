package ru.locationwatch.mobile_client.data

import retrofit2.HttpException
import ru.locationwatch.mobile_client.network.NotificationService
import ru.locationwatch.mobile_client.network.models.FirebaseTokenRequest

interface NotificationRepository {

    suspend fun sendFirebaseToken(token: String)

    suspend fun deleteFirebaseToken(token: String)

}

class NetworkNotificationRepository(
    private val notificationService: NotificationService
) : NotificationRepository {

    override suspend fun sendFirebaseToken(token: String) {
        val response = notificationService.sendFirebaseToken(FirebaseTokenRequest(token))
        return response.body() ?: throw HttpException(response)
    }

    override suspend fun deleteFirebaseToken(token: String) {
        val response = notificationService.deleteFirebaseToken(FirebaseTokenRequest(token))
        return response.body() ?: throw HttpException(response)
    }

}