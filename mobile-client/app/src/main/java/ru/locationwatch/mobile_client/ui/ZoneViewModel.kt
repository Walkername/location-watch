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
import ru.locationwatch.mobile_client.data.ZoneRepository
import ru.locationwatch.mobile_client.network.models.PersonErrorResponse
import ru.locationwatch.mobile_client.network.models.ZoneResponse
import java.io.IOException

sealed interface ZoneUiState {
    data class Success(val zones: List<ZoneResponse>) : ZoneUiState
    data class Error(val message: String) : ZoneUiState
    object Loading : ZoneUiState
}

class ZoneViewModel(
    private val zoneRepository: ZoneRepository
) : ViewModel() {

    var zoneUiState: ZoneUiState by mutableStateOf(ZoneUiState.Loading)
        private set

    fun fetchZones() {
        viewModelScope.launch {
            zoneUiState = try {
                ZoneUiState.Success(zoneRepository.getZones())
            } catch (e: IOException) {
                e.message?.let { Log.e("getZone", it) }
                ZoneUiState.Error("Network error")
            } catch (e: HttpException) {
                Log.e("getZones", e.message())
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = parseError(errorBody)
                if (errorResponse?.message != null) {
                    ZoneUiState.Error(errorResponse.message)
                } else {
                    ZoneUiState.Error("Getting zones error")
                }
            }
        }
    }

    companion object {
        fun createFactory(application: Application): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    val zoneRepository = (application as AuthApplication).container.zoneRepository
                    ZoneViewModel(
                        zoneRepository = zoneRepository
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