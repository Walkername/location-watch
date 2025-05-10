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
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.locationwatch.mobile_client.AuthApplication
import ru.locationwatch.mobile_client.data.AuthRepository
import ru.locationwatch.mobile_client.network.models.JWTResponse
import java.io.IOException

sealed interface LoginUiState {
    data class Success(val jwtResponse: JWTResponse) : LoginUiState
    object Error : LoginUiState
    object Loading : LoginUiState
}

sealed interface RegisterUiState {
    object Success : RegisterUiState
    data class Error(val message: String) : RegisterUiState
    object Loading : RegisterUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var loginUiState: LoginUiState by mutableStateOf(LoginUiState.Loading)
        private set

    var registerUiState: RegisterUiState by mutableStateOf(RegisterUiState.Loading)
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            loginUiState = LoginUiState.Loading
            loginUiState = try {
                LoginUiState.Success(authRepository.login(username, password))
            } catch (e: IOException) {
                e.message?.let { Log.e("log-in", it) }
                LoginUiState.Error
            } catch (e: HttpException) {
                Log.e("log-in", e.message())
                LoginUiState.Error
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
                RegisterUiState.Error("Server error")
            }
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
                    AuthViewModel(authRepository = authRepository)
                }
            }
        }
    }

}