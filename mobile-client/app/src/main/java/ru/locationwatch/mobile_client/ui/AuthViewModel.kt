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

sealed interface AuthUiState {
    data class Success(val jwtResponse: JWTResponse) : AuthUiState
    object Error : AuthUiState
    object Loading : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var authUiState: AuthUiState by mutableStateOf(AuthUiState.Loading)
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            authUiState = AuthUiState.Loading
            authUiState = try {
                AuthUiState.Success(authRepository.login(username, password))
            } catch (e: IOException) {
                e.message?.let { Log.e("log-in", it) }
                AuthUiState.Error
            } catch (e: HttpException) {
                Log.e("log-in", e.message())
                AuthUiState.Error
            }
        }
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