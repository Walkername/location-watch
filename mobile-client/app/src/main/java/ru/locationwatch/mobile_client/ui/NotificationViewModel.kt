package ru.locationwatch.mobile_client.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.locationwatch.mobile_client.AuthApplication
import ru.locationwatch.mobile_client.data.NotificationRepository
import ru.locationwatch.mobile_client.network.FCMTokenManager
import ru.locationwatch.mobile_client.network.models.PersonErrorResponse
import java.io.IOException

sealed interface TokenUiState {
    object Success : TokenUiState
    data class Error(val message: String) : TokenUiState
    object Loading : TokenUiState
}

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val fcmTokenManager: FCMTokenManager
) : ViewModel() {

    var tokenUiState: TokenUiState by mutableStateOf(TokenUiState.Loading)
        private set

    fun sendFirebaseToken(token: String) {
        viewModelScope.launch {
            tokenUiState = TokenUiState.Loading
            tokenUiState = try {
                notificationRepository.sendFirebaseToken(token)
                TokenUiState.Success
            } catch (e: IOException) {
                e.message?.let { Log.e("send-fb-token", it) }
                TokenUiState.Error("")
            } catch (e: HttpException) {
                Log.e("send-fb-token", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    TokenUiState.Error(errorResponse.message)
                } else {
                    TokenUiState.Error("Firebase token sending error")
                }
            }
        }
    }

    fun deleteFirebaseToken(token: String) {
        viewModelScope.launch {
            tokenUiState = TokenUiState.Loading
            tokenUiState = try {
                notificationRepository.deleteFirebaseToken(token)
                TokenUiState.Success
            } catch (e: IOException) {
                e.message?.let { Log.e("delete-fb-token", it) }
                TokenUiState.Error("")
            } catch (e: HttpException) {
                Log.e("delete-fb-token", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    TokenUiState.Error(errorResponse.message)
                } else {
                    TokenUiState.Error("Firebase token deleting error")
                }
            }
        }
    }

    fun getFcmToken(): String? {
        return fcmTokenManager.getFcmToken()
    }

    fun isTokenSent(): Boolean {
        return fcmTokenManager.isTokenSent()
    }

    fun markTokenStatus(status: Boolean) {
        fcmTokenManager.markTokenStatus(status)
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

    private fun parseError(errorBody: String?): PersonErrorResponse? {
        if (errorBody.isNullOrEmpty()) return null
        return try {
            Gson().fromJson(errorBody, PersonErrorResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

}