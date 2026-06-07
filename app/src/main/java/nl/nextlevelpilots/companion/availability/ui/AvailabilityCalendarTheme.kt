package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class CalendarDayStyle(
    val backgroundColor: Color,
    val textColor: Color,
)

@Immutable
data class AvailabilityCalendarTheme(
    val cardBackground: Color = Color.White,
    val weekdayLabelColor: Color = Color(0xFF3A3A3C),
    val selectedBorderColor: Color = Color(0xFFFF8B56),
    val selectedBorderWidth: Dp = 2.dp,
    val dayNumberFontSize: androidx.compose.ui.unit.TextUnit = 19.sp,
    val dayStyles: Map<CalendarDayDisplayState, CalendarDayStyle> = defaultDayStyles(),
    val cardShape: RoundedCornerShape = RoundedCornerShape(28.dp),
    val dayShape: RoundedCornerShape = RoundedCornerShape(18.dp),
    val dayCellHeight: Dp = 62.dp,
    val columnSpacing: Dp = 7.dp,
    val rowSpacing: Dp = 7.dp,
    val cardHorizontalPadding: Dp = 18.dp,
    val cardVerticalPadding: Dp = 30.dp,
    val weekdayLabelFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    val legendDotSize: Dp = 10.dp,
    val legendFontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
    val legendTextColor: Color = Color(0xFF3C3C43),
)

fun defaultDayStyles(): Map<CalendarDayDisplayState, CalendarDayStyle> {
    val availableStyle = CalendarDayStyle(
        backgroundColor = Color(0xFFDFF3E6),
        textColor = Color(0xFF137A3A),
    )
    val unavailableStyle = CalendarDayStyle(
        backgroundColor = Color(0xFFF8DEDE),
        textColor = Color(0xFFB3261E),
    )
    val maybeStyle = CalendarDayStyle(
        backgroundColor = Color(0xFFFFF1CC),
        textColor = Color(0xFFB77A00),
    )
    val partialStyle = maybeStyle

    return mapOf(
        CalendarDayDisplayState.AVAILABLE_FULL to availableStyle,
        CalendarDayDisplayState.UNAVAILABLE to unavailableStyle,
        CalendarDayDisplayState.MAYBE to maybeStyle,
        CalendarDayDisplayState.AVAILABLE_FROM to partialStyle,
        CalendarDayDisplayState.AVAILABLE_UNTIL to partialStyle,
        CalendarDayDisplayState.AVAILABLE_BETWEEN to partialStyle,
    )
}

fun AvailabilityCalendarTheme.styleFor(state: CalendarDayDisplayState): CalendarDayStyle {
    return dayStyles[state] ?: dayStyles.getValue(CalendarDayDisplayState.UNAVAILABLE)
}
