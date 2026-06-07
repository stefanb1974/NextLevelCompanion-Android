package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AvailabilityCalendarLegend(
    theme: AvailabilityCalendarTheme,
    modifier: Modifier = Modifier,
) {
    val availableStyle = theme.styleFor(CalendarDayDisplayState.AVAILABLE_FULL)
    val unavailableStyle = theme.styleFor(CalendarDayDisplayState.UNAVAILABLE)
    val maybeStyle = theme.styleFor(CalendarDayDisplayState.MAYBE)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LegendLine(
            dotColor = availableStyle.backgroundColor,
            label = "Beschikbaar",
            theme = theme,
        )
        LegendLine(
            dotColor = unavailableStyle.backgroundColor,
            label = "Niet beschikbaar",
            theme = theme,
        )
        LegendLine(
            dotColor = maybeStyle.backgroundColor,
            label = "Misschien beschikbaar",
            theme = theme,
        )
    }
}

@Composable
private fun LegendLine(
    dotColor: Color,
    label: String,
    theme: AvailabilityCalendarTheme,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(theme.legendDotSize)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text = label,
            color = theme.legendTextColor,
            fontSize = theme.legendFontSize,
            fontWeight = FontWeight.Medium,
        )
    }
}
