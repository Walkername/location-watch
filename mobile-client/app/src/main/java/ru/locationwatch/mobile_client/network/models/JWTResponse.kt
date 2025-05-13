package ru.locationwatch.mobile_client.network.models

data class JWTResponse(
    var accessToken: String? = null,
    var refreshToken: String? = null
)
