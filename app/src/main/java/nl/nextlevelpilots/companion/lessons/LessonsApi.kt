package nl.nextlevelpilots.companion.lessons

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LessonsApi {

    @GET("lessons")
    suspend fun getLessons(
        @Header("Authorization") authorization: String,
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<LessonsResponse>

    @POST("lessons/{id}/confirm")
    suspend fun confirmLesson(
        @Header("Authorization") authorization: String,
        @Path("id") lessonId: String,
    ): Response<LessonConfirmResponse>
}
