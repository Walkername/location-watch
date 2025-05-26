package ru.locationwatch.mobile_client.network.models

import kotlinx.serialization.Serializable

@Serializable
data class GPSDataRequest(
    var clientId: Int?,
    var latitude: Double?,
    var longitude: Double?,
    var speed: Double?
)
