package ru.locationwatch.mobile_client.network.models

import com.google.gson.annotations.SerializedName

data class JWTResponse(
    var accessToken: String? = null,
    var refreshToken: String? = null
)
