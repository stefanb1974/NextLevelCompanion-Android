package nl.nextlevelpilots.companion.availability.ui

import java.time.LocalDate

data class AvailabilityScreenInteractionState(
    val mode: CalendarInteractionMode = CalendarInteractionMode.SINGLE_DAY,
    val multiSelectedDays: Set<LocalDate> = emptySet(),
)

fun AvailabilityScreenInteractionState.withBulkEditEnabled(enabled: Boolean): AvailabilityScreenInteractionState {
    return if (enabled) {
        copy(mode = CalendarInteractionMode.MULTI_DAY)
    } else {
        copy(
            mode = CalendarInteractionMode.SINGLE_DAY,
            multiSelectedDays = emptySet(),
        )
    }
}

fun handleCalendarDayClick(
    date: LocalDate,
    interactionState: AvailabilityScreenInteractionState,
    onInteractionStateChange: (AvailabilityScreenInteractionState) -> Unit,
    onSingleDayTap: (LocalDate) -> Unit,
) {
    when (interactionState.mode) {
        CalendarInteractionMode.SINGLE_DAY -> onSingleDayTap(date)
        CalendarInteractionMode.MULTI_DAY -> {
            val updatedSelection = if (date in interactionState.multiSelectedDays) {
                interactionState.multiSelectedDays - date
            } else {
                interactionState.multiSelectedDays + date
            }
            onInteractionStateChange(
                interactionState.copy(multiSelectedDays = updatedSelection),
            )
        }
    }
}
