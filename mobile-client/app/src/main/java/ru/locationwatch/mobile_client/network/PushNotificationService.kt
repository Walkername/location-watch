package ru.locationwatch.mobile_client.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.locationwatch.mobile_client.AuthApplication
import java.util.Date

class PushNotificationService() : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("fb-token", "New FCM token: $token")

        // Saving firebase token in SharedPreferences
        val fcmTokenManager = FCMTokenManager(this)
        fcmTokenManager.saveToken(token)

        // Check if the user is already authorized
        // if so, then send fcm token to backend
        val tokenManager = TokenManager(this)
        val jwtToken = tokenManager.getAccessToken()

        if (jwtToken != null) {
            val expTime = tokenManager.getExpirationTime()
            val currentTime = Date().time

            expTime?.let {
                if (it * 1000 < currentTime) {
                    sendFcmToken(fcmTokenManager, token)
                }
            }
        }
    }

    private fun sendFcmToken(fcmTokenManager: FCMTokenManager, token: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val notificationRepository = (application as AuthApplication).container.notificationRepository
                notificationRepository.sendFirebaseToken(token)
                fcmTokenManager.markTokenAsSent()
            }
        } catch (e: Exception) {
            Log.e("fb-token", "Auto-send failed", e)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

    }

}