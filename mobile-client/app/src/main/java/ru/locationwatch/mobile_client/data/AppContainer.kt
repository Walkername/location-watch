package ru.locationwatch.mobile_client.data

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.locationwatch.mobile_client.network.AuthService
import ru.locationwatch.mobile_client.network.FCMTokenManager
import ru.locationwatch.mobile_client.network.NotificationService
import ru.locationwatch.mobile_client.network.TokenManager
import ru.locationwatch.mobile_client.network.UserService
import ru.locationwatch.mobile_client.network.ZoneService

interface AppContainer {

    val authRepository: AuthRepository

    val tokenManager: TokenManager

    val fcmTokenManager: FCMTokenManager

    val userRepository: UserRepository

    val zoneRepository: ZoneRepository

    val notificationRepository: NotificationRepository

}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val BASE_URL = "http://192.168.0.20:8080/"

    private val PUBLIC_URLS = listOf(
        "/auth/refresh"
    )

    private val tokenAuthenticator by lazy {
        object : Authenticator {
            override fun authenticate(route: Route?, response: Response): Request? {
                if (responseCount(response) > 1) return null

                val refreshToken = tokenManager.getRefreshToken() ?: return null

                return runBlocking {
                    try {
                        val jwtResponse = authRepository.refresh(refreshToken)
                        jwtResponse.accessToken?.let { newAccessToken ->
                            jwtResponse.refreshToken?.let { newRefreshToken ->
                                tokenManager.saveTokens(newAccessToken, newRefreshToken)
                            }
                            response.request().newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        }
                    } catch (e: Exception) {
                        tokenManager.clearTokens()
                        null
                    }
                }
            }

            private fun responseCount(response: Response): Int {
                var resp = response
                var count = 1
                while (resp.priorResponse() != null) {
                    count++
                    resp = resp.priorResponse()!!
                }
                return count
            }
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val request = originalRequest.newBuilder()
                    .apply {
                        if (!PUBLIC_URLS.contains(originalRequest.url().encodedPath())) {
                            tokenManager.getAccessToken()?.let {
                                header("Authorization", "Bearer $it")
                            }
                        }
                    }
                    .build()
                chain.proceed(request)
            }
            .authenticator(tokenAuthenticator)
            .build()
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .build()

    private val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(authService)
    }

    override val tokenManager: TokenManager by lazy {
        TokenManager(context)
    }

    override val fcmTokenManager: FCMTokenManager by lazy {
        FCMTokenManager(context)
    }

    private val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    override val userRepository: UserRepository by lazy {
        NetworkUserRepository(userService)
    }

    private val zoneService: ZoneService by lazy {
        retrofit.create(ZoneService::class.java)
    }

    override val zoneRepository: ZoneRepository by lazy {
        NetworkZoneRepository(zoneService)
    }

    private val notificationService: NotificationService by lazy {
        retrofit.create(NotificationService::class.java)
    }

    override val notificationRepository: NotificationRepository by lazy {
        NetworkNotificationRepository(notificationService)
    }

}