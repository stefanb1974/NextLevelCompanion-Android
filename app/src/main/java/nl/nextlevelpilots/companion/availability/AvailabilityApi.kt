package nl.nextlevelpilots.companion.availability

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AvailabilityApi {

    @GET("availability/me")
    suspend fun getMyAvailability(
        @Header("Authorization") authorization: String,
        @Query("month") month: String,
    ): Response<AvailabilityGetResponse>

    @POST("availability/me")
    suspend fun saveMyAvailability(
        @Header("Authorization") authorization: String,
        @Body body: SaveAvailabilityRequest,
    ): Response<AvailabilitySaveResponse>
}
