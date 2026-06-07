package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AvailabilityDayCell(
    day: CalendarDayUiModel,
    theme: AvailabilityCalendarTheme,
    interactionMode: CalendarInteractionMode,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: ((LocalDate) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (day.isPlaceholder || day.date == null || day.dayNumber == null) {
        Box(modifier = modifier)
        return
    }

    val date = day.date
    val style = theme.styleFor(day.displayState)
    val isHighlighted = when (interactionMode) {
        CalendarInteractionMode.SINGLE_DAY -> day.isSelected
        CalendarInteractionMode.MULTI_DAY -> day.isMultiSelectHighlighted
    }

    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "dayScale",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .clip(theme.dayShape)
            .background(style.backgroundColor)
            .then(
                if (isHighlighted) {
                    Modifier.border(
                        width = theme.selectedBorderWidth,
                        color = theme.selectedBorderColor,
                        shape = theme.dayShape,
                    )
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onDayClick(date) },
                onLongClick = {
                    if (interactionMode == CalendarInteractionMode.SINGLE_DAY) {
                        onDayLongClick?.invoke(date)
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 2.dp),
        ) {
            Text(
                text = day.dayNumber.toString(),
                color = style.textColor,
                fontSize = theme.dayNumberFontSize,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            if (!day.timeBadge.isNullOrBlank()) {
                Text(
                    text = day.timeBadge,
                    color = style.textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                )
            }
        }
    }
}
