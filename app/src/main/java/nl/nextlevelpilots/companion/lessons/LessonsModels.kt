package nl.nextlevelpilots.companion.lessons

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class LessonsResponse(
    val ok: Boolean? = null,
    val data: List<LessonDto>? = null,
    val error: String? = null,
)

data class LessonConfirmResponse(
    val ok: Boolean? = null,
    val data: Any? = null,
    val error: String? = null,
)

data class LessonDto(
    val id: String? = null,
    val date: String? = null,
    @SerializedName("start_time")
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    @SerializedName("lesson_type")
    val lessonType: String? = null,
    val location: String? = null,
    val status: String? = null,
    val notes: String? = null,
    @SerializedName("instructor_id")
    val instructorId: String? = null,
    val data: LessonDataDto? = null,
    @SerializedName(value = "additionalStudents", alternate = ["additional_students"])
    val additionalStudents: List<LessonParticipantDto>? = null,
    @SerializedName(value = "additionalInstructors", alternate = ["additional_instructors"])
    val additionalInstructors: List<LessonParticipantDto>? = null,
    val confirmations: JsonElement? = null,
)

data class LessonDataDto(
    @SerializedName("training_name")
    val trainingName: String? = null,
    @SerializedName("course_name")
    val courseName: String? = null,
    @SerializedName("trainee_name")
    val traineeName: String? = null,
    @SerializedName("instructor_name")
    val instructorName: String? = null,
    @SerializedName("training_id")
    val trainingId: String? = null,
    @SerializedName("course_id")
    val courseId: String? = null,
    @SerializedName("trainee_id")
    val traineeId: String? = null,
    @SerializedName("instructor_id")
    val instructorId: String? = null,
)

data class LessonParticipantDto(
    val id: String? = null,
    @SerializedName("student_id")
    val studentId: String? = null,
    @SerializedName("instructor_id")
    val instructorId: String? = null,
    val name: String? = null,
)

enum class LessonStatus {
    SUGGESTED,
    PLANNED,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    UNKNOWN,
}

data class LessonUiModel(
    val id: String,
    val apiId: String?,
    val date: LocalDate,
    val startTime: String?,
    val endTime: String?,
    val title: String?,
    val courseName: String?,
    val traineeName: String?,
    val instructorName: String?,
    val location: String?,
    val status: LessonStatus,
    val statusLabel: String,
    val isUserConfirmed: Boolean,
    val notes: String? = null,
    val additionalStudentNames: List<String> = emptyList(),
    val additionalInstructorNames: List<String> = emptyList(),
) {
    val timeRangeLabel: String = formatTimeRange(startTime, endTime)

    val debugTimeRange: String = formatDebugTimeRange(startTime, endTime)

    val isConfirmed: Boolean = isUserConfirmed

    val canConfirm: Boolean =
        !apiId.isNullOrBlank() &&
            !isUserConfirmed &&
            isLessonVisibleStatus(status)

    fun participantLabelForViewer(isInstructor: Boolean): String? {
        return if (isInstructor) traineeName else instructorName
    }
}

data class LessonDateGroup(
    val date: LocalDate,
    val dateLabel: String,
    val lessons: List<LessonUiModel>,
)

fun LessonDto.toLessonUiModel(linkedPersonId: String? = null): LessonUiModel? {
    val lessonDate = date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val apiId = id?.takeIf { it.isNotBlank() }
    val lessonId = apiId
        ?: listOf(
            lessonDate.toString(),
            resolvedStartTime(this),
            resolvedEndTime(this),
            resolvedTrainingName(this),
            lessonType,
        ).joinToString("|")
            .hashCode()
            .toString()

    val status = parseLessonStatus(this.status)
    val confirmationsMap = parseConfirmationsMap(confirmations)
    val isUserConfirmed = resolveUserConfirmation(confirmationsMap, linkedPersonId, status)

    return LessonUiModel(
        id = lessonId,
        apiId = apiId,
        date = lessonDate,
        startTime = resolvedStartTime(this),
        endTime = resolvedEndTime(this),
        title = resolvedTitle(this),
        courseName = resolvedCourseName(this),
        traineeName = resolvedTraineeName(this),
        instructorName = resolvedInstructorName(this),
        location = location?.takeIf { it.isNotBlank() },
        status = status,
        statusLabel = status.toDisplayLabel(),
        isUserConfirmed = isUserConfirmed,
        notes = notes?.takeIf { it.isNotBlank() },
        additionalStudentNames = resolveAdditionalStudentNames(this),
        additionalInstructorNames = resolveAdditionalInstructorNames(this),
    )
}

fun findLessonById(
    lessons: List<LessonUiModel>,
    lessonId: String,
): LessonUiModel? {
    return lessons.find { lesson ->
        lesson.id == lessonId || lesson.apiId == lessonId
    }
}

fun parseConfirmationsMap(element: JsonElement?): Map<String, String>? {
    if (element == null || element.isJsonNull) return null
    return when {
        element.isJsonObject -> {
            element.asJsonObject.entrySet().associate { (key, value) ->
                key to when {
                    value.isJsonPrimitive -> value.asString
                    else -> value.toString()
                }
            }
        }

        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
            val raw = element.asString
            if (raw.isBlank()) return null
            runCatching {
                parseConfirmationsMap(Gson().fromJson(raw, JsonElement::class.java))
            }.getOrNull()
        }

        else -> null
    }
}

fun resolveUserConfirmation(
    confirmations: Map<String, String>?,
    linkedPersonId: String?,
    lessonStatus: LessonStatus,
): Boolean {
    if (confirmations != null) {
        if (linkedPersonId.isNullOrBlank()) {
            return lessonStatus == LessonStatus.CONFIRMED
        }
        val userValue = confirmations[linkedPersonId]
        return userValue?.trim()?.equals("confirmed", ignoreCase = true) == true
    }
    return lessonStatus == LessonStatus.CONFIRMED
}

private fun resolvedStartTime(dto: LessonDto): String? =
    dto.startTime?.let(::normalizeLessonTime)

private fun resolvedEndTime(dto: LessonDto): String? =
    dto.endTime?.let(::normalizeLessonTime)

private fun resolvedTrainingName(dto: LessonDto): String? =
    dto.data?.trainingName?.takeIf { it.isNotBlank() }

private fun resolvedCourseName(dto: LessonDto): String? =
    dto.data?.courseName?.takeIf { it.isNotBlank() }

private fun resolvedTitle(dto: LessonDto): String? {
    return dto.data?.trainingName?.takeIf { it.isNotBlank() }
        ?: dto.lessonType?.takeIf { it.isNotBlank() }
}

private fun resolvedTraineeName(dto: LessonDto): String? {
    return dto.data?.traineeName?.takeIf { it.isNotBlank() }
        ?: dto.additionalStudents
            ?.firstNotNullOfOrNull { participant -> participant.name?.takeIf { it.isNotBlank() } }
}

private fun resolvedInstructorName(dto: LessonDto): String? {
    return dto.data?.instructorName?.takeIf { it.isNotBlank() }
        ?: dto.additionalInstructors
            ?.firstNotNullOfOrNull { participant -> participant.name?.takeIf { it.isNotBlank() } }
}

private fun resolveAdditionalStudentNames(dto: LessonDto): List<String> {
    return dto.additionalStudents
        ?.mapNotNull { participant -> participant.name?.takeIf { it.isNotBlank() } }
        .orEmpty()
}

private fun resolveAdditionalInstructorNames(dto: LessonDto): List<String> {
    return dto.additionalInstructors
        ?.mapNotNull { participant -> participant.name?.takeIf { it.isNotBlank() } }
        .orEmpty()
}

fun LessonDto.isAssignedToInstructor(linkedPersonId: String?): Boolean {
    if (linkedPersonId.isNullOrBlank()) return true
    if (instructorId == linkedPersonId) return true
    if (data?.instructorId == linkedPersonId) return true
    return additionalInstructors?.any { participant ->
        (participant.instructorId ?: participant.id) == linkedPersonId
    } == true
}

fun parseLessonStatus(raw: String?): LessonStatus {
    return when (raw?.trim()?.lowercase()) {
        "suggested" -> LessonStatus.SUGGESTED
        "planned", "option" -> LessonStatus.PLANNED
        "confirmed", "scheduled" -> LessonStatus.CONFIRMED
        "completed" -> LessonStatus.COMPLETED
        "cancelled", "canceled" -> LessonStatus.CANCELLED
        else -> LessonStatus.UNKNOWN
    }
}

fun isLessonVisibleStatus(status: LessonStatus): Boolean {
    return status == LessonStatus.PLANNED || status == LessonStatus.CONFIRMED
}

fun filterVisibleLessons(lessons: List<LessonUiModel>): List<LessonUiModel> {
    return lessons.filter { isLessonVisibleStatus(it.status) }
}

fun lessonEndDateTime(lesson: LessonUiModel): LocalDateTime {
    val time = lesson.endTime ?: lesson.startTime ?: "23:59"
    return LocalDateTime.of(lesson.date, parseLessonLocalTime(time))
}

fun lessonStartDateTime(lesson: LessonUiModel): LocalDateTime {
    val time = lesson.startTime ?: "00:00"
    return LocalDateTime.of(lesson.date, parseLessonLocalTime(time))
}

fun isLessonStillUpcoming(
    lesson: LessonUiModel,
    now: LocalDateTime = LocalDateTime.now(),
): Boolean {
    if (!isLessonVisibleStatus(lesson.status)) return false
    return !lessonEndDateTime(lesson).isBefore(now)
}

fun isLessonOngoing(
    lesson: LessonUiModel,
    now: LocalDateTime = LocalDateTime.now(),
): Boolean {
    if (!isLessonVisibleStatus(lesson.status)) return false
    val start = lessonStartDateTime(lesson)
    val end = lessonEndDateTime(lesson)
    return !now.isBefore(start) && !now.isAfter(end)
}

fun isLessonFuture(
    lesson: LessonUiModel,
    now: LocalDateTime = LocalDateTime.now(),
): Boolean {
    if (!isLessonVisibleStatus(lesson.status)) return false
    return lessonStartDateTime(lesson).isAfter(now)
}

fun findOngoingLesson(
    lessons: List<LessonUiModel>,
    now: LocalDateTime = LocalDateTime.now(),
): LessonUiModel? {
    return lessons
        .asSequence()
        .filter { isLessonOngoing(it, now) }
        .sortedWith(lessonTimeComparator)
        .firstOrNull()
}

fun findNextFutureLesson(
    lessons: List<LessonUiModel>,
    now: LocalDateTime = LocalDateTime.now(),
): LessonUiModel? {
    return lessons
        .asSequence()
        .filter { isLessonFuture(it, now) }
        .sortedWith(lessonTimeComparator)
        .firstOrNull()
}

enum class DashboardTrainingCardMode {
    ONGOING,
    UPCOMING,
    EMPTY,
}

fun resolveDashboardTrainingCard(
    visibleLessons: List<LessonUiModel>,
    now: LocalDateTime = LocalDateTime.now(),
): DashboardTrainingCardSelection {
    val ongoingLesson = findOngoingLesson(visibleLessons, now)
    val nextLesson = findNextFutureLesson(visibleLessons, now)
    val mode = when {
        ongoingLesson != null -> DashboardTrainingCardMode.ONGOING
        nextLesson != null -> DashboardTrainingCardMode.UPCOMING
        else -> DashboardTrainingCardMode.EMPTY
    }
    return DashboardTrainingCardSelection(
        ongoingLesson = ongoingLesson,
        nextLesson = nextLesson,
        mode = mode,
    )
}

data class DashboardTrainingCardSelection(
    val ongoingLesson: LessonUiModel?,
    val nextLesson: LessonUiModel?,
    val mode: DashboardTrainingCardMode,
) {
    val displayLesson: LessonUiModel?
        get() = ongoingLesson ?: nextLesson
}

fun formatLessonEndsIn(
    lesson: LessonUiModel,
    now: LocalDateTime = LocalDateTime.now(),
): String? {
    val end = lessonEndDateTime(lesson)
    if (!now.isBefore(end)) return null

    val totalMinutes = ChronoUnit.MINUTES.between(now, end)
    if (totalMinutes <= 0) return null

    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours == 0L -> {
            if (minutes == 1L) "Eindigt over 1 minuut" else "Eindigt over $minutes minuten"
        }

        minutes == 0L -> {
            if (hours == 1L) "Eindigt over 1 uur" else "Eindigt over $hours uur"
        }

        hours == 1L -> "Eindigt over 1 uur en $minutes minuten"

        else -> "Eindigt over $hours uur en $minutes minuten"
    }
}

private fun parseLessonLocalTime(raw: String): LocalTime {
    val normalized = normalizeLessonTime(raw)
    return LocalTime.parse(normalized, DateTimeFormatter.ofPattern("HH:mm"))
}

fun filterUpcomingLessons(
    lessons: List<LessonUiModel>,
    now: LocalDateTime = LocalDateTime.now(),
): List<LessonUiModel> {
    return lessons
        .asSequence()
        .filter(::isLessonStillUpcoming)
        .sortedWith(lessonTimeComparator)
        .toList()
}

fun LessonStatus.toDisplayLabel(): String {
    return when (this) {
        LessonStatus.SUGGESTED -> "Voorgesteld"
        LessonStatus.PLANNED -> "Gepland"
        LessonStatus.CONFIRMED -> "Bevestigd"
        LessonStatus.COMPLETED -> "Afgerond"
        LessonStatus.CANCELLED -> "Geannuleerd"
        LessonStatus.UNKNOWN -> "Gepland"
    }
}

fun normalizeLessonTime(raw: String): String {
    val parts = raw.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return String.format("%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

fun formatTimeRange(startTime: String?, endTime: String?): String {
    val start = startTime?.let(::formatLessonClock) ?: "—"
    val end = endTime?.let(::formatLessonClock) ?: "—"
    return "$start – $end"
}

fun formatDebugTimeRange(startTime: String?, endTime: String?): String {
    val start = startTime?.let(::formatLessonClock) ?: "—"
    val end = endTime?.let(::formatLessonClock) ?: "—"
    return "$start-$end"
}

fun formatLessonClock(time: String): String = normalizeLessonTime(time)

fun formatLessonDate(date: LocalDate, locale: Locale = Locale.forLanguageTag("nl-NL")): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", locale)
    return date.format(formatter).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
    }
}

fun formatLessonDateShort(date: LocalDate, locale: Locale = Locale.forLanguageTag("nl-NL")): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
    return date.format(formatter)
}

fun isInstructorRole(role: String?): Boolean {
    return role?.trim()?.lowercase() in setOf("instructor", "admin")
}

fun formatRelativeLessonDate(
    date: LocalDate,
    today: LocalDate = LocalDate.now(),
): String {
    val daysBetween = ChronoUnit.DAYS.between(today, date)
    return when (daysBetween) {
        0L -> "Vandaag"
        1L -> "Morgen"
        in 2L..Int.MAX_VALUE.toLong() -> "Over $daysBetween dagen"
        else -> formatLessonDate(date)
    }
}

fun groupLessonsByDate(lessons: List<LessonUiModel>): List<LessonDateGroup> {
    return lessons
        .groupBy { it.date }
        .toSortedMap()
        .map { (date, items) ->
            LessonDateGroup(
                date = date,
                dateLabel = formatLessonDate(date),
                lessons = items.sortedWith(lessonTimeComparator),
            )
        }
}

fun findNextUpcomingLesson(
    lessons: List<LessonUiModel>,
    now: LocalDateTime = LocalDateTime.now(),
): LessonUiModel? {
    return filterUpcomingLessons(lessons, now).firstOrNull()
}

private val lessonTimeComparator = compareBy<LessonUiModel>(
    { it.date },
    { it.startTime ?: "99:99" },
)
