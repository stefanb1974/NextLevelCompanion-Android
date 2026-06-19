package nl.nextlevelpilots.companion.trainingprogress

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import nl.nextlevelpilots.companion.network.ApiClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class TrainingProgressResponse(
    val ok: Boolean? = null,
    val role: String? = null,
    val data: JsonElement? = null,
    val courses: List<TrainingProgressCourseDto>? = null,
    val activeStudentCount: Int? = null,
    val error: String? = null,
)

data class TrainingProgressCourseDto(
    val id: String? = null,
    val courseId: String? = null,
    val courseName: String? = null,
    val progressPercent: Double? = null,
    val nextLesson: TrainingProgressNextLessonDto? = null,
    val latestFeedback: String? = null,
)

data class TrainingProgressNextLessonDto(
    val id: String? = null,
    val title: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
)

data class TrainingProgressCourseUiModel(
    val id: String,
    val courseName: String,
    val progressPercent: Int,
    val nextLessonTitle: String?,
    val nextLessonDateLabel: String?,
    val nextLessonTimeLabel: String?,
    val latestFeedback: String?,
)

fun parseTrainingProgressResponse(raw: String?): TrainingProgressResponse? {
    if (raw.isNullOrBlank()) return null

    return runCatching {
        val element = ApiClient.gson.fromJson(raw, JsonElement::class.java)
        when {
            element.isJsonArray -> {
                TrainingProgressResponse(
                    ok = true,
                    courses = element.asJsonArray.mapNotNull { it.toTrainingProgressCourseDto() },
                )
            }

            element.isJsonObject -> {
                val obj = element.asJsonObject
                val dataElement = obj.get("data")?.takeUnless { it.isJsonNull }
                val role = obj.stringValue("role")
                val parsedCourses: List<TrainingProgressCourseDto>
                val activeStudentCount: Int?

                when (role?.trim()?.lowercase()) {
                    "instructor" -> {
                        val students = dataElement?.parseInstructorStudents().orEmpty()
                        activeStudentCount = students.size
                        parsedCourses = students.flatMap { student ->
                            student.courses.map { course ->
                                course.copy(
                                    courseName = listOfNotNull(student.name, course.courseName)
                                        .joinToString(" – ")
                                        .ifBlank { course.courseName ?: "Training" },
                                )
                            }
                        }
                    }

                    else -> {
                        activeStudentCount = null
                        parsedCourses = when {
                            dataElement?.isJsonArray == true -> {
                                dataElement.asJsonArray.mapNotNull { it.toTrainingProgressCourseDto() }
                            }

                            dataElement?.isJsonObject == true -> {
                                dataElement.asJsonObject.parseCourseList().orEmpty()
                            }

                            else -> obj.parseCourseList().orEmpty()
                        }
                    }
                }

                TrainingProgressResponse(
                    ok = parseOkFlag(obj.get("ok")),
                    role = role,
                    data = dataElement,
                    courses = parsedCourses,
                    activeStudentCount = activeStudentCount,
                    error = obj.stringValue("error"),
                )
            }

            else -> null
        }
    }.getOrNull()
}

private fun JsonObject.parseCourseList(): List<TrainingProgressCourseDto>? {
    listOf("courses", "enrollments", "trainings", "items").forEach { key ->
        val array = get(key)?.takeUnless { it.isJsonNull } ?: return@forEach
        if (array.isJsonArray) {
            return array.asJsonArray.mapNotNull { it.toTrainingProgressCourseDto() }
        }
    }
    return null
}

private data class TrainingProgressStudentDto(
    val studentId: String?,
    val name: String?,
    val courses: List<TrainingProgressCourseDto>,
)

private fun JsonElement.parseInstructorStudents(): List<TrainingProgressStudentDto> {
    if (!isJsonObject) return emptyList()
    val studentsElement = asJsonObject.get("students")?.takeUnless { it.isJsonNull } ?: return emptyList()
    if (!studentsElement.isJsonArray) return emptyList()

    return studentsElement.asJsonArray.mapNotNull { element ->
        if (!element.isJsonObject) return@mapNotNull null
        val obj = element.asJsonObject
        TrainingProgressStudentDto(
            studentId = obj.stringValue("student_id") ?: obj.stringValue("studentId"),
            name = obj.stringValue("name"),
            courses = obj.parseCourseList().orEmpty(),
        )
    }
}

private fun JsonElement.toTrainingProgressCourseDto(): TrainingProgressCourseDto? {
    if (!isJsonObject) return null
    val obj = asJsonObject

    val nextLessonElement = obj.get("next_lesson")
        ?: obj.get("nextLesson")
        ?: obj.get("upcoming_lesson")
        ?: obj.get("upcomingLesson")

    return TrainingProgressCourseDto(
        id = obj.stringValue("id") ?: obj.stringValue("enrollment_id"),
        courseId = obj.stringValue("course_id") ?: obj.stringValue("courseId"),
        courseName = resolveCourseName(obj),
        progressPercent = resolveProgressPercent(obj),
        nextLesson = nextLessonElement?.toTrainingProgressNextLessonDto(),
        latestFeedback = obj.stringValue("latest_feedback")
            ?: obj.stringValue("latestFeedback")
            ?: obj.stringValue("feedback")
            ?: obj.stringValue("last_feedback"),
    )
}

private fun JsonElement.toTrainingProgressNextLessonDto(): TrainingProgressNextLessonDto? {
    if (!isJsonObject) return null
    val obj = asJsonObject
    return TrainingProgressNextLessonDto(
        id = obj.stringValue("id") ?: obj.stringValue("lesson_id"),
        title = obj.stringValue("title")
            ?: obj.stringValue("name")
            ?: obj.stringValue("lesson_type")
            ?: obj.stringValue("lessonType"),
        date = obj.stringValue("date"),
        startTime = obj.stringValue("start_time") ?: obj.stringValue("startTime"),
        endTime = obj.stringValue("end_time") ?: obj.stringValue("endTime"),
    )
}

private fun resolveCourseName(obj: JsonObject): String? {
    return obj.stringValue("course_name")
        ?: obj.stringValue("courseName")
        ?: obj.stringValue("training_name")
        ?: obj.stringValue("trainingName")
        ?: obj.stringValue("name")
        ?: obj.stringValue("title")
}

private fun resolveProgressPercent(obj: JsonObject): Double? {
    val keys = listOf(
        "progress_percent",
        "progress_percentage",
        "progressPercent",
        "progressPercentage",
        "percent",
        "percentage",
        "progress",
    )
    keys.forEach { key ->
        val value = obj.doubleValue(key) ?: return@forEach
        return if (value in 0.0..1.0) value * 100.0 else value
    }
    return null
}

fun TrainingProgressCourseDto.toUiModel(): TrainingProgressCourseUiModel? {
    val resolvedId = id?.takeIf { it.isNotBlank() }
        ?: courseId?.takeIf { it.isNotBlank() }
        ?: courseName?.takeIf { it.isNotBlank() }
        ?: return null

    val resolvedName = courseName?.takeIf { it.isNotBlank() } ?: "Training"
    val percent = progressPercent?.roundToInt()?.coerceIn(0, 100) ?: 0

    return TrainingProgressCourseUiModel(
        id = resolvedId,
        courseName = resolvedName,
        progressPercent = percent,
        nextLessonTitle = nextLesson?.title?.takeIf { it.isNotBlank() },
        nextLessonDateLabel = nextLesson?.date?.let(::formatTrainingProgressDate),
        nextLessonTimeLabel = formatTrainingProgressTimeRange(
            startTime = nextLesson?.startTime,
            endTime = nextLesson?.endTime,
        ),
        latestFeedback = latestFeedback?.takeIf { it.isNotBlank() },
    )
}

fun formatTrainingProgressDate(raw: String): String? {
    return runCatching {
        val date = when {
            raw.contains('T') -> LocalDate.parse(raw.substring(0, 10))
            else -> LocalDate.parse(raw.take(10))
        }
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("nl-NL"))
        date.format(formatter)
    }.getOrNull()
}

fun formatTrainingProgressTimeRange(startTime: String?, endTime: String?): String? {
    if (startTime.isNullOrBlank() && endTime.isNullOrBlank()) return null
    val start = startTime?.take(5) ?: "—"
    val end = endTime?.take(5) ?: "—"
    return "$start – $end"
}

private fun parseOkFlag(element: JsonElement?): Boolean? {
    if (element == null || element.isJsonNull) return null
    return when {
        element.isJsonPrimitive && element.asJsonPrimitive.isBoolean -> element.asBoolean
        element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asInt != 0
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
            element.asString.equals("true", ignoreCase = true) || element.asString == "1"
        }
        else -> null
    }
}

private fun JsonObject.stringValue(key: String): String? {
    val element = get(key) ?: return null
    if (element.isJsonNull) return null
    return when {
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> element.asString
        element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asJsonPrimitive.asString
        element.isJsonPrimitive && element.asJsonPrimitive.isBoolean -> element.asBoolean.toString()
        else -> null
    }?.takeIf { it.isNotBlank() }
}

private fun JsonObject.doubleValue(key: String): Double? {
    val element = get(key) ?: return null
    if (element.isJsonNull) return null
    return when {
        element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asDouble
        element.isJsonPrimitive && element.asJsonPrimitive.isString -> element.asString.toDoubleOrNull()
        else -> null
    }
}
