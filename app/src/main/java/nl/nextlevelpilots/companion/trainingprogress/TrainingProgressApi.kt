package nl.nextlevelpilots.companion.trainingprogress

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface TrainingProgressApi {

    @GET("training-progress/me")
    suspend fun getMyTrainingProgress(
        @Header("Authorization") authorization: String,
    ): Response<ResponseBody>
}
