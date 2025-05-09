package ru.locationwatch.mobile_client.config

import android.content.Context
import java.util.Properties

class AppConfig(private val context: Context) {

    private val properties: Properties by lazy {
        Properties().apply {
            context.assets.open("mqtt_config.properties").use { load(it) }
        }
    }

    val serverURI: String get() = properties.getProperty("mqtt.server.uri")
    val userId: String get() = properties.getProperty("mqtt.user.id")
    val userPassword: String get() = properties.getProperty("mqtt.user.password")

}