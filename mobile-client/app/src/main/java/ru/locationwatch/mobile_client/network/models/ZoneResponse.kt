package ru.locationwatch.mobile_client.network.models

data class ZoneResponse(
    var title: String? = null,
    var typeName: String? = null,
    var speed: Int? = null,
    var area: List<Coordinate>? = null
)
