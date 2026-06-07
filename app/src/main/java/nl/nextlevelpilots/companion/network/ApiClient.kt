package nl.nextlevelpilots.companion.network

import com.google.gson.Gson
import nl.nextlevelpilots.companion.auth.AuthApi
import nl.nextlevelpilots.companion.availability.AvailabilityApi
import nl.nextlevelpilots.companion.documents.DocumentsApi
import nl.nextlevelpilots.companion.lessons.LessonsApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    val gson: Gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ensureTrailingSlash(ApiConfig.BASE_URL))
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val availabilityApi: AvailabilityApi = retrofit.create(AvailabilityApi::class.java)
    val lessonsApi: LessonsApi = retrofit.create(LessonsApi::class.java)
    val documentsApi: DocumentsApi = retrofit.create(DocumentsApi::class.java)

    private fun ensureTrailingSlash(url: String): String =
        if (url.endsWith("/")) url else "$url/"
}
