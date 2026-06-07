package nl.nextlevelpilots.companion.documents

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Streaming

interface DocumentsApi {

    @GET("documents")
    suspend fun getDocuments(
        @Header("Authorization") authorization: String,
    ): Response<ResponseBody>

    @GET("documents/{id}/download")
    @Streaming
    suspend fun downloadDocument(
        @Header("Authorization") authorization: String,
        @Path("id") documentId: String,
    ): Response<ResponseBody>
}
