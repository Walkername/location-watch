package ru.locationwatch.mobile_client.network

import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.locationwatch.mobile_client.AuthApplication
import ru.locationwatch.mobile_client.MainActivity
import ru.locationwatch.mobile_client.R
import java.util.Date
import kotlin.random.Random

object NotificationManager {
    // Shared flow for notifications
    val notificationFlow = MutableSharedFlow<Pair<String?, String?>>()
}

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
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        // Emit notification to the shared flow
        CoroutineScope(Dispatchers.Main).launch {
            NotificationManager.notificationFlow.emit(Pair(title, body))
        }

        // Intent to open MainActivity with deep link action
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "OPEN_DETAILS_ACTION"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            putExtra(
                "ntf_title",
                remoteMessage.notification?.title ?:
                remoteMessage.data["ntf_title"])
            putExtra(
                "ntf_body",
                remoteMessage.notification?.body ?:
                remoteMessage.data["ntf_body"])

            // Add custom data
            remoteMessage.data.forEach { (key, value) ->
                if (key != "ntf_title" && key != "ntf_body") {
                    putExtra(key, value)
                }
            }

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // PendingIntent for notification tap
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val channelId = "alert_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setSmallIcon(R.drawable.gps_loc)
            .setContentIntent(pendingIntent) // Open app on tap
            .setAutoCancel(true)
            .build()

        // Show notification
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Alerts", IMPORTANCE_HIGH)
            )
        }
        manager.notify(Random.nextInt(), notification)
    }

}