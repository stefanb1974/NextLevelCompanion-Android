package nl.nextlevelpilots.companion.lessons

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
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
    val data: LessonDataDto? = null,
    val additionalStudents: List<LessonParticipantDto>? = null,
    val additionalInstructors: List<LessonParticipantDto>? = null,
    val confirmations: JsonElement? = null,
)

data class LessonDataDto(
    val trainingName: String? = null,
    val courseName: String? = null,
    val traineeName: String? = null,
    val instructorName: String? = null,
    val trainingId: String? = null,
    val courseId: String? = null,
    val traineeId: String? = null,
    val instructorId: String? = null,
)

data class LessonParticipantDto(
    val id: String? = null,
    val studentId: String? = null,
    val instructorId: String? = null,
    val name: String? = null,
)

enum class LessonStatus {
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
            status != LessonStatus.CANCELLED &&
            status != LessonStatus.COMPLETED

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

fun parseLessonStatus(raw: String?): LessonStatus {
    return when (raw?.trim()?.lowercase()) {
        "suggested", "planned", "option" -> LessonStatus.PLANNED
        "confirmed", "scheduled" -> LessonStatus.CONFIRMED
        "completed" -> LessonStatus.COMPLETED
        "cancelled", "canceled" -> LessonStatus.CANCELLED
        else -> LessonStatus.UNKNOWN
    }
}

fun isInstructorRole(role: String?): Boolean {
    return role?.trim()?.lowercase() in setOf("instructor", "admin")
}

fun filterUpcomingLessons(
    lessons: List<LessonUiModel>,
    today: LocalDate = LocalDate.now(),
): List<LessonUiModel> {
    return lessons
        .asSequence()
        .filter { !it.date.isBefore(today) }
        .filter { it.status != LessonStatus.CANCELLED }
        .filter { it.status != LessonStatus.COMPLETED }
        .sortedWith(lessonTimeComparator)
        .toList()
}

fun LessonStatus.toDisplayLabel(): String {
    return when (this) {
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
    today: LocalDate = LocalDate.now(),
): LessonUiModel? {
    return filterUpcomingLessons(lessons, today).firstOrNull()
}

private val lessonTimeComparator = compareBy<LessonUiModel>(
    { it.date },
    { it.startTime ?: "99:99" },
)
