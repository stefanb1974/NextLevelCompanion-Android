package nl.nextlevelpilots.companion.lessons

import android.util.Log
import nl.nextlevelpilots.companion.auth.SessionStore
import nl.nextlevelpilots.companion.BuildConfig
import nl.nextlevelpilots.companion.network.ApiClient
import nl.nextlevelpilots.companion.network.ApiConfig
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
        val userRole = sessionStore.currentUserRole()

        return try {
            logDebug("API base URL: ${ApiConfig.BASE_URL} (BuildConfig: ${BuildConfig.API_BASE_URL})")
            logDebug("Session role=$userRole linked_person_id=$linkedPersonId")
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
                logRawLessons(rawEntries, linkedPersonId)

                val visibleEntries = if (isInstructorRole(userRole)) {
                    rawEntries.filter { entry ->
                        val assigned = entry.isAssignedToInstructor(linkedPersonId)
                        logInstructorAssignment(entry, linkedPersonId, assigned)
                        assigned
                    }
                } else {
                    rawEntries
                }
                logDebug("Lessons after instructor filter: ${visibleEntries.size} of ${rawEntries.size}")

                val lessons = visibleEntries
                    .mapNotNull { entry ->
                        entry.toLessonUiModel(linkedPersonId).also { lesson ->
                            if (lesson == null) {
                                logDebug("Skipped unmapped lesson entry: ${bodyToJson(entry)}")
                            }
                        }
                    }
                    .sortedWith(compareBy({ it.date }, { it.startTime ?: "99:99" }))

                logMappedLessons(lessons)

                val visibleLessons = filterVisibleLessons(lessons)
                val upcoming = filterUpcomingLessons(visibleLessons)
                logDebug(
                    "Lessons pipeline: raw=${rawEntries.size}, " +
                        "afterInstructor=${visibleEntries.size}, " +
                        "mapped=${lessons.size}, " +
                        "visibleAfterStatus=${visibleLessons.size}, " +
                        "upcoming=${upcoming.size}",
                )
                upcoming.firstOrNull()?.let { next ->
                    logDebug(
                        "Next lesson: id=${next.apiId ?: next.id}, " +
                            "date=${next.date}, time=${next.debugTimeRange}, status=${next.status}",
                    )
                }

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

    private fun logRawLessons(entries: List<LessonDto>, linkedPersonId: String?) {
        Log.d(DEBUG_TAG, "Received ${entries.size} raw lesson(s) from API")
        entries.forEachIndexed { index, entry ->
            val additionalInstructorIds = entry.additionalInstructors
                ?.mapNotNull { participant ->
                    participant.instructorId ?: participant.id
                }
                .orEmpty()
            Log.d(
                DEBUG_TAG,
                buildString {
                    append("lesson[$index] ")
                    append("id=${entry.id ?: "null"}, ")
                    append("date=${entry.date ?: "null"}, ")
                    append("start_time=${entry.startTime ?: "null"}, ")
                    append("end_time=${entry.endTime ?: "null"}, ")
                    append("status=${entry.status ?: "null"}, ")
                    append("instructor_id=${entry.instructorId ?: entry.data?.instructorId ?: "null"}, ")
                    append("additionalInstructors=$additionalInstructorIds")
                },
            )
        }
    }

    private fun logInstructorAssignment(
        entry: LessonDto,
        linkedPersonId: String?,
        assigned: Boolean,
    ) {
        val additionalInstructorIds = entry.additionalInstructors
            ?.mapNotNull { participant ->
                participant.instructorId ?: participant.id
            }
            .orEmpty()
        Log.d(
            DEBUG_TAG,
            buildString {
                append("instructorAssignment lesson=${entry.id ?: "null"}: ")
                append("linkedPersonId=${linkedPersonId ?: "null"}, ")
                append("topLevelInstructorId=${entry.instructorId ?: "null"}, ")
                append("dataInstructorId=${entry.data?.instructorId ?: "null"}, ")
                append("additionalInstructors=$additionalInstructorIds, ")
                append("assigned=$assigned")
            },
        )
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
