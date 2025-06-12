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
import ru.locationwatch.mobile_client.data.AuthRepository
import ru.locationwatch.mobile_client.network.TokenManager
import ru.locationwatch.mobile_client.network.models.JWTResponse
import ru.locationwatch.mobile_client.network.models.PersonErrorResponse
import java.io.IOException

sealed interface LoginUiState {
    data class Success(val jwtResponse: JWTResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
    object Loading : LoginUiState
}

sealed interface RegisterUiState {
    object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
    object Loading : RegisterUiState
}

sealed interface RefreshUiState {
    object Success : RefreshUiState
    data class Error(val message: String) : RefreshUiState
    object Loading : RefreshUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Loading)
        private set

    var registerUiState: RegisterUiState by mutableStateOf(RegisterUiState.Loading)
        private set

    var refreshUiState: RefreshUiState by mutableStateOf(RefreshUiState.Loading)
        private set

    var userId by mutableStateOf<Int?>(null)
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginUiState = LoginUiState.Loading
            loginUiState = try {
                val response = authRepository.login(username, password)
                response.accessToken?.let { access ->
                    response.refreshToken?.let { refresh ->
                        tokenManager.saveTokens(access, refresh)
                    }
                }
                updateUserId()
                LoginUiState.Success(response)
            } catch (e: IOException) {
                e.message?.let { Log.e("log-in", it) }
                LoginUiState.Error("")
            } catch (e: HttpException) {
                Log.e("log-in", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    LoginUiState.Error(errorResponse.message)
                } else {
                    LoginUiState.Error("Login error")
                }

            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            registerUiState = RegisterUiState.Loading
            registerUiState = try {
                authRepository.register(username, password)
                RegisterUiState.Success
            } catch (e: IOException) {
                e.message?.let { Log.e("register", it) }
                RegisterUiState.Error("Network error")
            } catch (e: HttpException) {
                Log.e("register", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    RegisterUiState.Error(errorResponse.message)
                } else {
                    RegisterUiState.Error("Register error")
                }
            }
        }
    }

    fun refreshTokens(refreshToken: String) {
        viewModelScope.launch {
            refreshUiState = RefreshUiState.Loading
            refreshUiState = try {
                val response = authRepository.refresh(refreshToken)
                response.accessToken?.let { access ->
                    response.refreshToken?.let { refresh ->
                        tokenManager.saveTokens(access, refresh)
                    }
                }
                updateUserId()
                RefreshUiState.Success
            } catch (e: IOException) {
                e.message?.let { Log.e("refresh tokens", it) }
                RefreshUiState.Error("Network error")
            } catch (e: HttpException) {
                Log.e("refresh tokens", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    RefreshUiState.Error(errorResponse.message)
                } else {
                    RefreshUiState.Error("Refresh tokens error")
                }
            }
        }
    }

    fun getAccessExpirationTime(): Long? {
        return tokenManager.getAccessExpirationTime()
    }

    fun getRefreshExpirationTime(): Long? {
        return tokenManager.getRefreshExpirationTime()
    }

    fun resetTokens() {
        tokenManager.clearTokens()
    }

    fun getAccessToken(): String? {
        return tokenManager.getAccessToken()
    }

    fun getRefreshToken(): String? {
        return tokenManager.getRefreshToken()
    }

    fun loadUserId() {
        userId = tokenManager.getUserId()
    }

    fun updateUserId() {
        viewModelScope.launch {
            userId = tokenManager.getUserId()
        }
    }

    fun resetRegisterState() {
        registerUiState = RegisterUiState.Loading
    }

    fun resetLoginState() {
        loginUiState = LoginUiState.Loading
    }

    companion object {
        fun createFactory(application: Application): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    val authRepository = (application as AuthApplication).container.authRepository
                    val tokenManager = application.container.tokenManager
                    AuthViewModel(
                        authRepository = authRepository,
                        tokenManager = tokenManager
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