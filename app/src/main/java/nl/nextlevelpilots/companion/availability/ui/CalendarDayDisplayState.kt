package nl.nextlevelpilots.companion.availability.ui

/**
 * Visual states for a calendar day cell.
 * The grid renders solely from this enum — new availability types only need mapping + theme entries.
 */
enum class CalendarDayDisplayState {
    AVAILABLE_FULL,
    UNAVAILABLE,
    MAYBE,
    AVAILABLE_FROM,
    AVAILABLE_UNTIL,
    AVAILABLE_BETWEEN,
}
