package nl.nextlevelpilots.companion.documents

import android.content.Context
import android.util.Log
import nl.nextlevelpilots.companion.auth.SessionStore
import nl.nextlevelpilots.companion.network.ApiClient
import okhttp3.ResponseBody
import java.io.File

class DocumentsRepository(
    private val sessionStore: SessionStore,
    private val documentsApi: DocumentsApi = ApiClient.documentsApi,
) {

    sealed class LoadResult {
        data class Success(val documents: List<DocumentUiModel>) : LoadResult()

        data class Error(
            val technicalMessage: String,
            val userMessage: String = LOAD_ERROR_MESSAGE,
        ) : LoadResult()
    }

    sealed class DownloadResult {
        data class Success(
            val file: File,
            val mimeType: String,
        ) : DownloadResult()

        data class Error(
            val technicalMessage: String,
            val userMessage: String = DOWNLOAD_ERROR_MESSAGE,
        ) : DownloadResult()
    }

    suspend fun loadDocuments(): LoadResult {
        val token = sessionStore.currentToken()
            ?: return LoadResult.Error(
                technicalMessage = "No auth token in session",
                userMessage = LOAD_ERROR_MESSAGE,
            )

        return try {
            val response = documentsApi.getDocuments(authorization = bearer(token))
            val requestUrl = response.raw().request.url.toString()
            val responseCode = response.code()

            logDebug("loadDocuments request URL: $requestUrl")
            val rawBody = response.body()?.string() ?: response.errorBody()?.string()

            logDebug("loadDocuments response code: $responseCode")
            logDebug("loadDocuments response body: ${rawBody ?: "null"}")

            val parsed = parseDocumentsResponse(rawBody)

            if (response.isSuccessful && parsed?.ok != false) {
                val rawEntries = parsed?.documentEntries().orEmpty()
                val documents = rawEntries
                    .mapNotNull { entry ->
                        entry.toDocumentUiModel().also { mapped ->
                            if (mapped == null) {
                                logDebug("Skipped unmapped document entry: ${bodyToJson(entry)}")
                            }
                        }
                    }
                    .sortedBy { it.title.lowercase() }

                logDebug("loadDocuments mapped document count: ${documents.size}")
                LoadResult.Success(documents = documents)
            } else {
                val technicalMessage = parsed?.error ?: rawBody ?: "HTTP $responseCode"
                logBackendError("loadDocuments", technicalMessage, responseCode)
                LoadResult.Error(technicalMessage = technicalMessage)
            }
        } catch (e: Exception) {
            logBackendError("loadDocuments", e.message ?: e.toString())
            LoadResult.Error(technicalMessage = e.message ?: e.toString())
        }
    }

    suspend fun downloadDocument(
        documentId: String,
        cacheDir: File,
    ): DownloadResult {
        val token = sessionStore.currentToken()
            ?: return DownloadResult.Error(
                technicalMessage = "No auth token in session",
                userMessage = DOWNLOAD_ERROR_MESSAGE,
            )

        return try {
            val response = documentsApi.downloadDocument(
                authorization = bearer(token),
                documentId = documentId,
            )
            val requestUrl = response.raw().request.url.toString()
            val responseCode = response.code()

            logDebug("downloadDocument request URL: $requestUrl")
            logDebug("downloadDocument response code: $responseCode")

            if (!response.isSuccessful) {
                val errorBodyRaw = response.errorBody()?.string()
                logDebug("downloadDocument response body: ${errorBodyRaw ?: "null"}")
                val technicalMessage = errorBodyRaw ?: "HTTP $responseCode"
                logBackendError("downloadDocument", technicalMessage, responseCode)
                return DownloadResult.Error(technicalMessage = technicalMessage)
            }

            val body = response.body()
                ?: return DownloadResult.Error(technicalMessage = "Empty download response body")

            val mimeType = response.headers()["Content-Type"]?.substringBefore(";")?.trim()
                ?: "application/pdf"
            val extension = extensionForMimeType(mimeType)
            val outputFile = File(cacheDir, "document-$documentId.$extension")

            body.use { responseBody ->
                writeResponseBodyToFile(responseBody, outputFile)
            }

            logDebug("downloadDocument saved: ${outputFile.absolutePath}")
            DownloadResult.Success(file = outputFile, mimeType = mimeType)
        } catch (e: Exception) {
            logBackendError("downloadDocument", e.message ?: e.toString())
            DownloadResult.Error(technicalMessage = e.message ?: e.toString())
        }
    }

    private fun writeResponseBodyToFile(body: ResponseBody, file: File) {
        body.byteStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun extensionForMimeType(mimeType: String): String {
        return when (mimeType.lowercase()) {
            "application/pdf" -> "pdf"
            "image/png" -> "png"
            "image/jpeg" -> "jpg"
            else -> "bin"
        }
    }

    private fun bearer(token: String): String = "Bearer $token"

    private fun logDebug(message: String) {
        Log.d(DEBUG_TAG, message)
    }

    private fun logBackendError(
        operation: String,
        technicalMessage: String,
        httpCode: Int? = null,
    ) {
        val codeSuffix = httpCode?.let { " (HTTP $it)" }.orEmpty()
        Log.w(TAG, "$operation failed$codeSuffix: $technicalMessage")
    }

    private fun bodyToJson(value: Any?): String {
        if (value == null) return "null"
        return runCatching { ApiClient.gson.toJson(value) }.getOrElse { value.toString() }
    }

    companion object {
        private const val TAG = "DocumentsRepository"
        private const val DEBUG_TAG = "DocumentsDebug"
        const val LOAD_ERROR_MESSAGE = "Documenten konden niet worden geladen."
        const val DOWNLOAD_ERROR_MESSAGE = "Document kon niet worden geopend."

        fun documentsCacheDir(context: Context): File {
            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }
    }
}
