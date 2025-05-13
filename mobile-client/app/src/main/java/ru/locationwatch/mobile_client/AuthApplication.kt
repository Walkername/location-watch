package ru.locationwatch.mobile_client

import android.app.Application
import ru.locationwatch.mobile_client.data.AppContainer
import ru.locationwatch.mobile_client.data.DefaultAppContainer

class AuthApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

}