package nl.nextlevelpilots.companion.trainingprogress

import android.util.Log
import nl.nextlevelpilots.companion.auth.SessionStore
import nl.nextlevelpilots.companion.network.ApiClient

class TrainingProgressRepository(
    private val sessionStore: SessionStore,
    private val trainingProgressApi: TrainingProgressApi = ApiClient.trainingProgressApi,
) {

    sealed class LoadResult {
        data class Success(val courses: List<TrainingProgressCourseUiModel>) : LoadResult()

        data class Error(
            val technicalMessage: String,
            val userMessage: String = LOAD_ERROR_MESSAGE,
        ) : LoadResult()
    }

    suspend fun loadTrainingProgress(): LoadResult {
        val token = sessionStore.currentToken()
            ?: return LoadResult.Error(
                technicalMessage = "No auth token in session",
                userMessage = LOAD_ERROR_MESSAGE,
            )

        return try {
            val response = trainingProgressApi.getMyTrainingProgress(authorization = bearer(token))
            val requestUrl = response.raw().request.url.toString()
            val responseCode = response.code()
            val rawBody = response.body()?.string() ?: response.errorBody()?.string()

            logDebug("getMyTrainingProgress request URL: $requestUrl")
            logDebug("getMyTrainingProgress response code: $responseCode")
            logDebug("getMyTrainingProgress response body: ${rawBody ?: "null"}")

            val parsed = parseTrainingProgressResponse(rawBody)

            if (response.isSuccessful && parsed?.ok != false) {
                val courses = parsed?.courses.orEmpty()
                    .mapNotNull { dto ->
                        dto.toUiModel().also { mapped ->
                            if (mapped == null) {
                                logDebug("Skipped unmapped training progress entry")
                            }
                        }
                    }

                logDebug("getMyTrainingProgress mapped course count: ${courses.size}")
                LoadResult.Success(courses = courses)
            } else {
                val technicalMessage = parsed?.error ?: rawBody ?: "HTTP $responseCode"
                logBackendError("getMyTrainingProgress", technicalMessage, responseCode)
                LoadResult.Error(technicalMessage = technicalMessage)
            }
        } catch (e: Exception) {
            logBackendError("getMyTrainingProgress", e.message ?: e.toString())
            LoadResult.Error(technicalMessage = e.message ?: e.toString())
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

    companion object {
        private const val TAG = "TrainingProgressRepository"
        private const val DEBUG_TAG = "TrainingProgressDebug"
        const val LOAD_ERROR_MESSAGE = "Trainingsvoortgang kon niet worden geladen."
    }
}
