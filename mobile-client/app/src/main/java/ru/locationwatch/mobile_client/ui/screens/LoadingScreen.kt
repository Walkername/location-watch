package ru.locationwatch.mobile_client.ui.screens

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import ru.locationwatch.mobile_client.ui.AuthViewModel
import ru.locationwatch.mobile_client.ui.RefreshUiState
import java.util.Date

@Composable
fun LoadingScreen(
    navigateToMain: () -> Unit,
    navigateToAuth: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val viewModelFactory = AuthViewModel.createFactory(app)
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)

    val accessToken = viewModel.getAccessToken()
    val accessExpirationTime = viewModel.getAccessExpirationTime()
    val refreshToken = viewModel.getRefreshToken()
    val refreshExpirationTime = viewModel.getRefreshExpirationTime()
    val refreshUiState = viewModel.refreshUiState
    val currentTime = Date().time

    val authStatus = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (accessToken != null) {
            accessExpirationTime?.let { accessExpTime ->
                if (accessExpTime * 1000 > currentTime) {
                    authStatus.value = true
                } else {
                    Log.e("token", "access token is expired")
                    authStatus.value = false

                    // If refreshToken is also expired, then authStatus = false
                    if (refreshToken != null) {
                        refreshExpirationTime?.let { refreshExpTime ->
                            if (refreshExpTime * 1000 > currentTime) {
                                Log.e("refresh-token", "refresh token is valid")
                                // send request to refresh tokens
                                viewModel.refreshTokens(refreshToken)
                            } else { // refresh token is expired
                                authStatus.value = false
                                viewModel.resetTokens()
                                delay(1000)
                                navigateToAuth()
                                Log.e("refresh-token", "refresh token is expired")
                            }
                        }
                    } else {
                        delay(1000)
                        navigateToAuth()
                    }
                }
            }
        } else {
            delay(1000)
            navigateToAuth()
        }
    }

    when (refreshUiState) {
        is RefreshUiState.Loading -> {
        }

        is RefreshUiState.Success -> {
            authStatus.value = true
            Log.e("refresh-success", "tokens were updated")
            LaunchedEffect(Unit) {
                delay(1000)
                navigateToMain()
            }
        }

        is RefreshUiState.Error -> {
            authStatus.value = false
            Log.e("refresh-error", refreshUiState.message)
            LaunchedEffect(Unit) {
                delay(1000)
                navigateToAuth()
            }
        }
    }

    Log.e("auth-status", "status: ${authStatus.value}")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Text("Loading...")
    }
}