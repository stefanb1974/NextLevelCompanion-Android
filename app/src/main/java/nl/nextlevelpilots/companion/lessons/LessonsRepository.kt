package nl.nextlevelpilots.companion.lessons

import android.util.Log
import nl.nextlevelpilots.companion.auth.SessionStore
import nl.nextlevelpilots.companion.network.ApiClient
import java.time.LocalDate

class LessonsRepository(
    private val sessionStore: SessionStore,
    private val lessonsApi: LessonsApi = ApiClient.lessonsApi,
) {

    sealed class LoadResult {
        data class Success(val lessons: List<LessonUiModel>) : LoadResult()

        data class Error(
            val technicalMessage: String,
            val userMessage: String = LOAD_ERROR_MESSAGE,
        ) : LoadResult()
    }

    suspend fun loadLessons(
        from: LocalDate = defaultFromDate(),
        to: LocalDate = defaultToDate(),
    ): LoadResult {
        val token = sessionStore.currentToken()
            ?: return LoadResult.Error(
                technicalMessage = "No auth token in session",
                userMessage = LOAD_ERROR_MESSAGE,
            )

        val fromKey = from.toString()
        val toKey = to.toString()
        val linkedPersonId = sessionStore.currentLinkedPersonId()

        return try {
            logDebug("loadLessons request: from=$fromKey to=$toKey")

            val response = lessonsApi.getLessons(
                authorization = bearer(token),
                from = fromKey,
                to = toKey,
            )
            val errorBodyRaw = if (!response.isSuccessful) response.errorBody()?.string() else null
            val body = response.body() ?: parseError(errorBodyRaw)

            logDebug(
                "loadLessons response (http=${response.code()}): ${bodyToJson(body)}",
            )

            if (response.isSuccessful && body?.ok != false) {
                val rawEntries = body?.data.orEmpty()
                logRawLessons(rawEntries)

                val lessons = rawEntries
                    .mapNotNull { entry ->
                        entry.toLessonUiModel(linkedPersonId).also { lesson ->
                            if (lesson == null) {
                                logDebug("Skipped unmapped lesson entry: ${bodyToJson(entry)}")
                            }
                        }
                    }
                    .sortedWith(compareBy({ it.date }, { it.startTime ?: "99:99" }))

                logMappedLessons(lessons)
                LoadResult.Success(lessons = lessons)
            } else {
                val technicalMessage = body?.error ?: errorBodyRaw ?: "HTTP ${response.code()}"
                logBackendError("loadLessons", technicalMessage, response.code())
                LoadResult.Error(technicalMessage = technicalMessage)
            }
        } catch (e: Exception) {
            logBackendError("loadLessons", e.message ?: e.toString())
            LoadResult.Error(technicalMessage = e.message ?: e.toString())
        }
    }

    private fun logRawLessons(entries: List<LessonDto>) {
        Log.d(DEBUG_TAG, "Received ${entries.size} raw lesson(s) from API")
        entries.forEachIndexed { index, entry ->
            Log.d(DEBUG_TAG, "LESSON RAW:\n${bodyToJson(entry)}")
            Log.d(
                DEBUG_TAG,
                buildString {
                    append("lesson[$index] ")
                    append("id=${entry.id ?: "null"}, ")
                    append("title=${entry.data?.trainingName ?: entry.lessonType ?: "null"}, ")
                    append("course=${entry.data?.courseName ?: "null"}, ")
                    append("module=${entry.lessonType ?: "null"}, ")
                    append("start_time=${entry.startTime ?: "null"}, ")
                    append("end_time=${entry.endTime ?: "null"}, ")
                    append("status=${entry.status ?: "null"}, ")
                    append("confirmations=${bodyToJson(entry.confirmations)}, ")
                    append("location=${entry.location ?: "null"}")
                },
            )
        }
    }

    suspend fun confirmLesson(lessonId: String): Result<Unit> {
        val token = sessionStore.currentToken()
            ?: return Result.failure(IllegalStateException("No auth token in session"))

        return try {
            logDebug("confirmLesson request: lessonId=$lessonId")

            val response = lessonsApi.confirmLesson(
                authorization = bearer(token),
                lessonId = lessonId,
            )
            val errorBodyRaw = if (!response.isSuccessful) response.errorBody()?.string() else null
            val body = response.body() ?: parseConfirmError(errorBodyRaw)

            logDebug(
                "confirmLesson response (lessonId=$lessonId, http=${response.code()}): ${bodyToJson(body)}",
            )

            if (response.isSuccessful && body?.ok != false) {
                Result.success(Unit)
            } else {
                val technicalMessage = body?.error ?: errorBodyRaw ?: "HTTP ${response.code()}"
                logBackendError("confirmLesson", technicalMessage, response.code())
                Result.failure(Exception(technicalMessage))
            }
        } catch (e: Exception) {
            logBackendError("confirmLesson", e.message ?: e.toString())
            Result.failure(e)
        }
    }

    private fun logMappedLessons(lessons: List<LessonUiModel>) {
        lessons.forEach { lesson ->
            Log.d(
                DEBUG_TAG,
                buildString {
                    appendLine("Mapped lesson:")
                    appendLine("time=${lesson.debugTimeRange}")
                    appendLine("title=${lesson.title ?: "null"}")
                    appendLine("course=${lesson.courseName ?: "null"}")
                    appendLine("trainee=${lesson.traineeName ?: "null"}")
                    appendLine("instructor=${lesson.instructorName ?: "null"}")
                    appendLine("location=${lesson.location ?: "null"}")
                    append("userConfirmed=${lesson.isUserConfirmed}")
                },
            )
        }
    }

    private fun bearer(token: String): String = "Bearer $token"

    private fun logDebug(message: String) {
        Log.d(TAG, message)
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

    private fun parseError(raw: String?): LessonsResponse? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            ApiClient.gson.fromJson(raw, LessonsResponse::class.java)
        }.getOrNull()
    }

    private fun parseConfirmError(raw: String?): LessonConfirmResponse? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            ApiClient.gson.fromJson(raw, LessonConfirmResponse::class.java)
        }.getOrNull()
    }

    companion object {
        private const val TAG = "LessonsRepository"
        private const val DEBUG_TAG = "LessonsDebug"
        const val LOAD_ERROR_MESSAGE = "Trainingen konden niet worden geladen."
        const val CONFIRM_ERROR_MESSAGE = "Bevestigen mislukt"
        const val CONFIRM_SUCCESS_MESSAGE = "Training bevestigd"

        fun defaultFromDate(today: LocalDate = LocalDate.now()): LocalDate =
            today.minusDays(7)

        fun defaultToDate(today: LocalDate = LocalDate.now()): LocalDate =
            today.plusDays(90)
    }
}
