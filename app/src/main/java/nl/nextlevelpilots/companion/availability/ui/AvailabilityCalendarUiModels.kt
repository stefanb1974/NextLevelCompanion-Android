package nl.nextlevelpilots.companion.availability.ui

import java.time.LocalDate
import java.time.YearMonth

enum class CalendarInteractionMode {
    SINGLE_DAY,
    MULTI_DAY,
}

data class AvailabilityCalendarGridState(
    val month: YearMonth,
    val weeks: List<CalendarWeekUiModel>,
    val weekdayLabels: List<String> = AvailabilityCalendarDefaults.weekdayLabels,
)

data class CalendarWeekUiModel(
    val days: List<CalendarDayUiModel>,
) {
    init {
        require(days.size == AvailabilityCalendarDefaults.columns) {
            "Calendar weeks must contain exactly ${AvailabilityCalendarDefaults.columns} cells."
        }
    }
}

data class CalendarDayUiModel(
    val date: LocalDate?,
    val dayNumber: Int?,
    val displayState: CalendarDayDisplayState = CalendarDayDisplayState.UNAVAILABLE,
    val timeBadge: String? = null,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isDirty: Boolean = false,
    val isMultiSelectHighlighted: Boolean = false,
) {
    val isPlaceholder: Boolean = date == null || dayNumber == null
}

data class CalendarDaySource(
    val date: LocalDate,
    val availability: nl.nextlevelpilots.companion.availability.DayAvailability,
)

object AvailabilityCalendarDefaults {
    const val columns = 7
    val weekdayLabels = listOf("ma", "di", "wo", "do", "vr", "za", "zo")
}
