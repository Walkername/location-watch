package ru.locationwatch.mobile_client.data

import retrofit2.HttpException
import ru.locationwatch.mobile_client.network.ZoneService
import ru.locationwatch.mobile_client.network.models.ZoneResponse

interface ZoneRepository {

    suspend fun getZones() : List<ZoneResponse>

}

class NetworkZoneRepository(
    private val zoneService: ZoneService
) : ZoneRepository {

    override suspend fun getZones(): List<ZoneResponse> {
        val response = zoneService.getZones()
        return response.body() ?: throw HttpException(response)
    }

}