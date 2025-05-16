package ru.locationwatch.mobile_client.network

import retrofit2.Response
import retrofit2.http.GET
import ru.locationwatch.mobile_client.network.models.ZoneResponse

interface ZoneService {

    @GET("/zones")
    suspend fun getZones(): Response<List<ZoneResponse>>

}