package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate

/**
 * Stable 7-column calendar grid. Extend behavior via [CalendarInteractionMode] and
 * [AvailabilityCalendarGridState] — do not change cell layout when adding features.
 */
@Composable
fun AvailabilityCalendarGrid(
    state: AvailabilityCalendarGridState,
    theme: AvailabilityCalendarTheme,
    interactionMode: CalendarInteractionMode,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: ((LocalDate) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(theme.rowSpacing),
    ) {
        CalendarWeekdayHeader(
            labels = state.weekdayLabels,
            theme = theme,
        )

        state.weeks.forEach { week ->
            CalendarWeekRow(
                week = week,
                theme = theme,
                interactionMode = interactionMode,
                onDayClick = onDayClick,
                onDayLongClick = onDayLongClick,
            )
        }
    }
}

@Composable
private fun CalendarWeekdayHeader(
    labels: List<String>,
    theme: AvailabilityCalendarTheme,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.columnSpacing),
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                color = theme.weekdayLabelColor,
                fontSize = theme.weekdayLabelFontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalendarWeekRow(
    week: CalendarWeekUiModel,
    theme: AvailabilityCalendarTheme,
    interactionMode: CalendarInteractionMode,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: ((LocalDate) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(theme.dayCellHeight),
        horizontalArrangement = Arrangement.spacedBy(theme.columnSpacing),
    ) {
        week.days.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AvailabilityDayCell(
                    day = day,
                    theme = theme,
                    interactionMode = interactionMode,
                    onDayClick = onDayClick,
                    onDayLongClick = onDayLongClick,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
