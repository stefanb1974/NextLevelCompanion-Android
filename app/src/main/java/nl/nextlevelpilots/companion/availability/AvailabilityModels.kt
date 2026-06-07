package nl.nextlevelpilots.companion.availability

import java.time.LocalDate
import java.time.YearMonth

enum class DayAvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
    MAYBE,
    NOT_SET,
}

data class DayAvailability(
    val status: DayAvailabilityStatus,
    val startTime: String? = null,
    val endTime: String? = null,
) {
    fun onSingleTap(): DayAvailability {
        return when (status) {
            DayAvailabilityStatus.UNAVAILABLE,
            DayAvailabilityStatus.NOT_SET,
            -> availableFullDay()

            DayAvailabilityStatus.AVAILABLE -> maybeFullDay()
            DayAvailabilityStatus.MAYBE -> unavailable()
        }
    }

    companion object {
        fun notSet(): DayAvailability = DayAvailability(DayAvailabilityStatus.NOT_SET)

        fun unavailable(): DayAvailability = DayAvailability(DayAvailabilityStatus.UNAVAILABLE)

        fun availableFullDay(): DayAvailability = DayAvailability(DayAvailabilityStatus.AVAILABLE)

        fun maybeFullDay(): DayAvailability = DayAvailability(DayAvailabilityStatus.MAYBE)

        fun availableFrom(time: String): DayAvailability = DayAvailability(
            status = DayAvailabilityStatus.AVAILABLE,
            startTime = normalizeTime(time),
            endTime = null,
        )

        fun availableUntil(time: String): DayAvailability = DayAvailability(
            status = DayAvailabilityStatus.AVAILABLE,
            startTime = null,
            endTime = normalizeTime(time),
        )

        fun availableBetween(start: String, end: String): DayAvailability = DayAvailability(
            status = DayAvailabilityStatus.AVAILABLE,
            startTime = normalizeTime(start),
            endTime = normalizeTime(end),
        )
    }
}

data class AvailabilityGetResponse(
    val ok: Boolean? = null,
    val data: AvailabilityData? = null,
    val error: String? = null,
)

data class AvailabilityData(
    val availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>>? = null,
    val availability: Map<String, AvailabilityDayEntry>? = null,
    val month: String? = null,
)

data class AvailabilityDayEntry(
    val date: String? = null,
    val status: String? = null,
    val timeBlock: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
)

data class SaveAvailabilityRequest(
    val month: String,
    val availability: Map<String, AvailabilityDayEntry>,
)

data class AvailabilitySaveResponse(
    val ok: Boolean? = null,
    val data: AvailabilityData? = null,
    val error: String? = null,
)

fun YearMonth.toApiKey(): String =
    String.format("%04d-%02d", year, monthValue)

fun LocalDate.toApiKey(): String =
    String.format("%04d-%02d-%02d", year, monthValue, dayOfMonth)

fun AvailabilityData.toAvailabilityByMonth(
    requestedMonth: YearMonth,
): Map<String, Map<String, AvailabilityDayEntry>> {
    if (!availabilityByMonth.isNullOrEmpty()) {
        return availabilityByMonth
    }

    if (!availability.isNullOrEmpty()) {
        val monthKey = month?.takeIf { it.isNotBlank() } ?: requestedMonth.toApiKey()
        return mapOf(monthKey to availability)
    }

    return emptyMap()
}

fun AvailabilityDayEntry.toDayAvailability(): DayAvailability {
    val normalizedStatus = status
        ?.trim()
        ?.lowercase()
        ?.replace("-", "_")
        ?.replace(" ", "_")

    return when (normalizedStatus) {
        "available" -> {
            val start = startTime?.let(::normalizeTime)
            val end = endTime?.let(::normalizeTime)
            val hasStart = !start.isNullOrBlank()
            val hasEnd = !end.isNullOrBlank()
            val isFullDay = timeBlock == "full-day" || (!hasStart && !hasEnd)

            when {
                hasStart && hasEnd -> DayAvailability.availableBetween(start!!, end!!)
                hasStart -> DayAvailability.availableFrom(start!!)
                hasEnd -> DayAvailability.availableUntil(end!!)
                isFullDay -> DayAvailability.availableFullDay()
                else -> DayAvailability.availableFullDay()
            }
        }

        "not_available", "unavailable", "notavailable" -> DayAvailability.unavailable()
        "maybe" -> DayAvailability.maybeFullDay()
        else -> DayAvailability.notSet()
    }
}

fun DayAvailability.toDebugLabel(): String {
    return when (status) {
        DayAvailabilityStatus.AVAILABLE -> when {
            !startTime.isNullOrBlank() && !endTime.isNullOrBlank() -> "available_between($startTime-$endTime)"
            !startTime.isNullOrBlank() -> "available_from($startTime)"
            !endTime.isNullOrBlank() -> "available_until($endTime)"
            else -> "available_full"
        }

        DayAvailabilityStatus.UNAVAILABLE -> "not_available"
        DayAvailabilityStatus.MAYBE -> "maybe"
        DayAvailabilityStatus.NOT_SET -> "not_set"
    }
}

fun parseAvailabilityDate(dateKey: String, entryDate: String?): LocalDate? {
    return runCatching { LocalDate.parse(dateKey) }.getOrNull()
        ?: entryDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}

@Deprecated("Use toDayAvailability()", ReplaceWith("toDayAvailability().status"))
fun AvailabilityDayEntry.toDayStatus(): DayAvailabilityStatus = toDayAvailability().status

fun DayAvailability.toDayEntry(date: LocalDate): AvailabilityDayEntry? {
    return when (status) {
        DayAvailabilityStatus.NOT_SET -> null
        DayAvailabilityStatus.UNAVAILABLE -> AvailabilityDayEntry(
            date = date.toApiKey(),
            status = "not_available",
            timeBlock = null,
            startTime = null,
            endTime = null,
        )

        DayAvailabilityStatus.MAYBE -> AvailabilityDayEntry(
            date = date.toApiKey(),
            status = "maybe",
            timeBlock = "full-day",
            startTime = null,
            endTime = null,
        )

        DayAvailabilityStatus.AVAILABLE -> {
            val start = startTime?.let(::normalizeTime)
            val end = endTime?.let(::normalizeTime)
            val hasStart = !start.isNullOrBlank()
            val hasEnd = !end.isNullOrBlank()

            when {
                hasStart && hasEnd -> AvailabilityDayEntry(
                    date = date.toApiKey(),
                    status = "available",
                    timeBlock = null,
                    startTime = start,
                    endTime = end,
                )

                hasStart -> AvailabilityDayEntry(
                    date = date.toApiKey(),
                    status = "available",
                    timeBlock = null,
                    startTime = start,
                    endTime = null,
                )

                hasEnd -> AvailabilityDayEntry(
                    date = date.toApiKey(),
                    status = "available",
                    timeBlock = null,
                    startTime = null,
                    endTime = end,
                )

                else -> AvailabilityDayEntry(
                    date = date.toApiKey(),
                    status = "available",
                    timeBlock = "full-day",
                    startTime = null,
                    endTime = null,
                )
            }
        }
    }
}

fun DayAvailabilityStatus.toFullDayAvailability(): DayAvailability {
    return when (this) {
        DayAvailabilityStatus.AVAILABLE -> DayAvailability.availableFullDay()
        DayAvailabilityStatus.UNAVAILABLE -> DayAvailability.unavailable()
        DayAvailabilityStatus.MAYBE -> DayAvailability.maybeFullDay()
        DayAvailabilityStatus.NOT_SET -> DayAvailability.notSet()
    }
}

fun normalizeTime(raw: String): String {
    val parts = raw.trim().split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return formatTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

fun formatTime(hour: Int, minute: Int): String =
    String.format("%02d:%02d", hour, minute)

fun parseTime(time: String): Pair<Int, Int> {
    val normalized = normalizeTime(time)
    val parts = normalized.split(":")
    return parts[0].toInt() to parts[1].toInt()
}

fun timeHourLabel(time: String?): String? {
    if (time.isNullOrBlank()) return null
    val hour = time.substringBefore(":").toIntOrNull() ?: return null
    return hour.toString()
}
