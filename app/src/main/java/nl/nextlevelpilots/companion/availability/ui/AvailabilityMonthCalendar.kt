package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth

/**
 * Month container for the calendar card.
 *
 * Swipe navigation: replace [AnimatedMonthTransition] internals with [HorizontalPager]
 * while keeping [AvailabilityCalendarGrid] unchanged.
 */
@Composable
fun AvailabilityMonthCalendar(
    gridState: AvailabilityCalendarGridState,
    theme: AvailabilityCalendarTheme,
    interactionMode: CalendarInteractionMode,
    onDayClick: (LocalDate) -> Unit,
    onDayLongClick: ((LocalDate) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onSwipePreviousMonth: (() -> Unit)? = null,
    onSwipeNextMonth: (() -> Unit)? = null,
) {
    AnimatedMonthTransition(
        month = gridState.month,
        modifier = modifier,
        onSwipePreviousMonth = onSwipePreviousMonth,
        onSwipeNextMonth = onSwipeNextMonth,
    ) {
        AvailabilityCalendarCard(
            theme = theme,
            content = {
                AvailabilityCalendarGrid(
                    state = gridState,
                    theme = theme,
                    interactionMode = interactionMode,
                    onDayClick = onDayClick,
                    onDayLongClick = onDayLongClick,
                )
            },
        )
    }
}

@Composable
private fun AnimatedMonthTransition(
    month: YearMonth,
    modifier: Modifier = Modifier,
    onSwipePreviousMonth: (() -> Unit)?,
    onSwipeNextMonth: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    // Swipe hooks reserved for a future HorizontalPager integration.
    @Suppress("UNUSED_VARIABLE")
    val swipePrevious = onSwipePreviousMonth
    @Suppress("UNUSED_VARIABLE")
    val swipeNext = onSwipeNextMonth

    AnimatedContent(
        targetState = month,
        modifier = modifier,
        transitionSpec = {
            fadeIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) togetherWith fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        },
        label = "availabilityMonthTransition",
    ) {
        content()
    }
}

@Composable
fun AvailabilityCalendarCard(
    theme: AvailabilityCalendarTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = theme.cardShape,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.07f),
            ),
        shape = theme.cardShape,
        colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = theme.cardHorizontalPadding,
                    vertical = theme.cardVerticalPadding,
                ),
        ) {
            content()
        }
    }
}
