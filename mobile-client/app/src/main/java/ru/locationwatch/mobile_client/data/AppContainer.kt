package ru.locationwatch.mobile_client.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.locationwatch.mobile_client.network.AuthService

interface AppContainer {

    val authRepository: AuthRepository

}

class DefaultAppContainer() : AppContainer {
    private val BASE_URL = "http://192.168.0.20:8080/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        NetworkAuthRepository(authService)
    }
}