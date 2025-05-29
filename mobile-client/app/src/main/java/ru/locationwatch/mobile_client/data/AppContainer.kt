package ru.locationwatch.mobile_client.data

import android.content.Context
import okhttp3.OkHttpClient
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

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .apply {
                        tokenManager.getAccessToken()?.let {
                            header("Authorization", "Bearer $it")
                        }
                    }
                    .build()
                chain.proceed(request)
            }
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