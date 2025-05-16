package ru.locationwatch.mobile_client.network.models

data class ZoneResponse(
    var typeName: String? = null,
    var area: List<Coordinate>? = null
)
