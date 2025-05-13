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
import ru.locationwatch.mobile_client.data.UserRepository
import ru.locationwatch.mobile_client.network.models.PersonErrorResponse
import ru.locationwatch.mobile_client.network.models.UserResponse
import java.io.IOException

sealed interface UserUiState {
    data class Success(val user: UserResponse) : UserUiState
    data class Error(val message: String) : UserUiState
    object Loading : UserUiState
}

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var userUiState: UserUiState by mutableStateOf(UserUiState.Loading)
        private set

    fun fetchUser(id: Int) {
        viewModelScope.launch {
            userUiState = UserUiState.Loading
            userUiState = try {
                UserUiState.Success(userRepository.getUser(id))
            } catch (e: IOException) {
                e.message?.let { Log.e("getUser", it) }
                UserUiState.Error("Network error")
            } catch (e: HttpException) {
                Log.e("getUser", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    UserUiState.Error(errorResponse.message)
                } else {
                    UserUiState.Error("Register error")
                }
            }
        }
    }

    companion object {
        fun createFactory(application: Application): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    val userRepository = (application as AuthApplication).container.userRepository
                    UserViewModel(
                        userRepository = userRepository
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