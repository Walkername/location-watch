package ru.locationwatch.mobile_client.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import ru.locationwatch.mobile_client.AuthApplication
import ru.locationwatch.mobile_client.data.NotificationRepository
import ru.locationwatch.mobile_client.network.FCMTokenManager

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val fcmTokenManager: FCMTokenManager
) : ViewModel() {

    fun sendFirebaseToken(token: String) {
        viewModelScope.launch {
            notificationRepository.sendFirebaseToken(token)
        }
    }

    fun getFcmToken(): String? {
        return fcmTokenManager.getFcmToken()
    }

    fun isTokenSent(): Boolean {
        return fcmTokenManager.isTokenSent()
    }

    fun markTokenAsSent() {
        fcmTokenManager.markTokenAsSent()
    }

    fun clearToken() {
        fcmTokenManager.clearToken()
    }

    fun getTokenAndStatus(): Pair<String?, Boolean> {
        return fcmTokenManager.getTokenAndStatus()
    }

    companion object {
        fun createFactory(application: Application): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    val notificationRepository = (application as AuthApplication).container.notificationRepository
                    val fcmTokenManager = application.container.fcmTokenManager
                    NotificationViewModel(
                        notificationRepository = notificationRepository,
                        fcmTokenManager = fcmTokenManager
                    )
                }
            }
        }
    }

}