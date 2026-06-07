package nl.nextlevelpilots.companion.availability.ui

import nl.nextlevelpilots.companion.availability.CalendarCell
import nl.nextlevelpilots.companion.availability.DayAvailability
import nl.nextlevelpilots.companion.availability.DayAvailabilityStatus
import nl.nextlevelpilots.companion.availability.timeHourLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

object AvailabilityCalendarUiMapper {

    fun mapGrid(
        month: YearMonth,
        cells: List<CalendarCell>,
        days: Map<LocalDate, DayAvailability>,
        savedDays: Map<LocalDate, DayAvailability>,
        selectedDay: LocalDate?,
        multiSelectedDays: Set<LocalDate> = emptySet(),
        today: LocalDate = LocalDate.now(),
    ): AvailabilityCalendarGridState {
        val dayModels = cells.map { cell ->
            if (cell.date == null || cell.dayNumber == null) {
                CalendarDayUiModel(date = null, dayNumber = null)
            } else {
                val date = cell.date
                val availability = days[date] ?: DayAvailability.notSet()
                val savedAvailability = savedDays[date] ?: DayAvailability.notSet()
                val displayState = mapDisplayState(
                    CalendarDaySource(date = date, availability = availability),
                )
                CalendarDayUiModel(
                    date = date,
                    dayNumber = cell.dayNumber,
                    displayState = displayState,
                    timeBadge = timeBadgeFor(displayState, availability),
                    isToday = date == today && YearMonth.from(date) == month,
                    isSelected = selectedDay == date,
                    isDirty = availability != savedAvailability,
                    isMultiSelectHighlighted = date in multiSelectedDays,
                )
            }
        }

        return AvailabilityCalendarGridState(
            month = month,
            weeks = dayModels.chunked(AvailabilityCalendarDefaults.columns).map { week ->
                CalendarWeekUiModel(days = week)
            },
        )
    }

    /**
     * Builds the fixed 7-column month grid. Use this for month swipe / pager without ViewModel changes.
     */
    fun buildMonthCells(month: YearMonth): List<CalendarCell> {
        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
        val leadingEmptyDays = firstDay.get(weekFields.dayOfWeek()) - 1

        val cells = mutableListOf<CalendarCell>()
        repeat(leadingEmptyDays) {
            cells += CalendarCell(date = null, dayNumber = null)
        }
        for (day in 1..lastDay.dayOfMonth) {
            cells += CalendarCell(
                date = month.atDay(day),
                dayNumber = day,
            )
        }
        while (cells.size % AvailabilityCalendarDefaults.columns != 0) {
            cells += CalendarCell(date = null, dayNumber = null)
        }
        return cells
    }

    fun mapGridForMonth(
        month: YearMonth,
        days: Map<LocalDate, DayAvailability>,
        savedDays: Map<LocalDate, DayAvailability>,
        selectedDay: LocalDate?,
        multiSelectedDays: Set<LocalDate> = emptySet(),
        today: LocalDate = LocalDate.now(),
    ): AvailabilityCalendarGridState {
        return mapGrid(
            month = month,
            cells = buildMonthCells(month),
            days = days,
            savedDays = savedDays,
            selectedDay = selectedDay,
            multiSelectedDays = multiSelectedDays,
            today = today,
        )
    }

    fun mapDisplayState(source: CalendarDaySource): CalendarDayDisplayState {
        return when (source.availability.status) {
            DayAvailabilityStatus.UNAVAILABLE,
            DayAvailabilityStatus.NOT_SET,
            -> CalendarDayDisplayState.UNAVAILABLE

            DayAvailabilityStatus.MAYBE -> CalendarDayDisplayState.MAYBE
            DayAvailabilityStatus.AVAILABLE -> resolveAvailableDisplayState(source.availability)
        }
    }

    private fun resolveAvailableDisplayState(availability: DayAvailability): CalendarDayDisplayState {
        val hasStart = !availability.startTime.isNullOrBlank()
        val hasEnd = !availability.endTime.isNullOrBlank()

        return when {
            hasStart && hasEnd -> CalendarDayDisplayState.AVAILABLE_BETWEEN
            hasStart -> CalendarDayDisplayState.AVAILABLE_FROM
            hasEnd -> CalendarDayDisplayState.AVAILABLE_UNTIL
            else -> CalendarDayDisplayState.AVAILABLE_FULL
        }
    }

    private fun timeBadgeFor(
        displayState: CalendarDayDisplayState,
        availability: DayAvailability,
    ): String? {
        return when (displayState) {
            CalendarDayDisplayState.AVAILABLE_FROM -> {
                timeHourLabel(availability.startTime)?.let { "> $it" }
            }

            CalendarDayDisplayState.AVAILABLE_UNTIL -> {
                timeHourLabel(availability.endTime)?.let { "< $it" }
            }

            CalendarDayDisplayState.AVAILABLE_BETWEEN -> {
                val start = timeHourLabel(availability.startTime)
                val end = timeHourLabel(availability.endTime)
                if (start != null && end != null) "$start-$end" else null
            }

            else -> null
        }
    }
}
