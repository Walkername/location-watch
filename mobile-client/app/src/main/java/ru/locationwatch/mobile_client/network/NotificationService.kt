package ru.locationwatch.mobile_client.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import ru.locationwatch.mobile_client.network.models.FirebaseTokenRequest

/**
 * This service is needed to send firebase token to backend
 */

interface NotificationService {

    @POST("/notifications/send-token")
    suspend fun sendFirebaseToken(
        @Body firebaseToken: FirebaseTokenRequest
    ) : Response<Unit>

}