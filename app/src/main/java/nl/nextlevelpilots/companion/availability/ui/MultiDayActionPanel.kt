package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.availability.DayAvailabilityStatus

@Composable
fun MultiDayActionPanel(
    visible: Boolean,
    selectedCount: Int,
    theme: AvailabilityCalendarTheme,
    onApplyStatus: (DayAvailabilityStatus) -> Unit,
    onOpenTimeEditor: () -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(20.dp)
    val availableStyle = theme.styleFor(CalendarDayDisplayState.AVAILABLE_FULL)
    val unavailableStyle = theme.styleFor(CalendarDayDisplayState.UNAVAILABLE)
    val maybeStyle = theme.styleFor(CalendarDayDisplayState.MAYBE)

    AnimatedVisibility(
        visible = visible && selectedCount > 0,
        modifier = modifier,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 130.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = cardShape,
                    ambientColor = Color.Black.copy(alpha = 0.04f),
                    spotColor = Color.Black.copy(alpha = 0.06f),
                ),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "$selectedCount dagen geselecteerd",
                        color = Color(0xFF1C1C1E),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(end = 36.dp),
                    )
                    IconButton(
                        onClick = onClearSelection,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Selectie wissen",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CompactStatusButton(
                        label = "Beschikbaar",
                        backgroundColor = availableStyle.backgroundColor,
                        textColor = availableStyle.textColor,
                        onClick = { onApplyStatus(DayAvailabilityStatus.AVAILABLE) },
                        modifier = Modifier.weight(1f),
                    )
                    CompactStatusButton(
                        label = "Misschien",
                        backgroundColor = maybeStyle.backgroundColor,
                        textColor = maybeStyle.textColor,
                        onClick = { onApplyStatus(DayAvailabilityStatus.MAYBE) },
                        modifier = Modifier.weight(1f),
                    )
                    CompactStatusButton(
                        label = "Niet beschikbaar",
                        backgroundColor = unavailableStyle.backgroundColor,
                        textColor = unavailableStyle.textColor,
                        onClick = { onApplyStatus(DayAvailabilityStatus.UNAVAILABLE) },
                        modifier = Modifier.weight(1f),
                    )
                    CompactStatusButton(
                        label = "Tijd",
                        backgroundColor = availableStyle.backgroundColor,
                        textColor = availableStyle.textColor,
                        onClick = onOpenTimeEditor,
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    text = "Kies een status of tijd voor alle geselecteerde dagen",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun CompactStatusButton(
    label: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)

    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 13.sp,
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 2.dp, vertical = 10.dp),
    )
}
