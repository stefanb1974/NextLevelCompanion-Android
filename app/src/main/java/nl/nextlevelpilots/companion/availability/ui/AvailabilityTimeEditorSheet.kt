package nl.nextlevelpilots.companion.availability.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.availability.DayAvailability
import nl.nextlevelpilots.companion.availability.DayAvailabilityStatus
import nl.nextlevelpilots.companion.availability.formatTime
import nl.nextlevelpilots.companion.availability.parseTime

private val TextPrimary = Color(0xFF1C1C1E)
private val TextSecondary = Color(0xFF8E8E93)
private val AccentOrange = Color(0xFFFF7A3D)
private val CardWhite = Color.White
private val TimeFieldBackground = Color(0xFFF4F5F8)

private enum class TimeEditorOption {
    FULL_DAY,
    UNAVAILABLE,
    MAYBE,
    FROM,
    UNTIL,
    BETWEEN,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityTimeEditorSheet(
    initialAvailability: DayAvailability?,
    theme: AvailabilityCalendarTheme,
    onDismiss: () -> Unit,
    onApply: (DayAvailability) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val initialOption = remember(initialAvailability) {
        optionFromAvailability(initialAvailability)
    }
    var selectedOption by remember(initialAvailability) { mutableStateOf(initialOption) }

    val defaultFrom = remember { "12:00" }
    val defaultUntil = remember { "16:00" }
    val defaultBetweenStart = remember { "09:00" }
    val defaultBetweenEnd = remember { "17:00" }

    var fromTime by remember(initialAvailability) {
        mutableStateOf(initialAvailability?.startTime ?: defaultFrom)
    }
    var untilTime by remember(initialAvailability) {
        mutableStateOf(initialAvailability?.endTime ?: defaultUntil)
    }
    var betweenStart by remember(initialAvailability) {
        mutableStateOf(
            if (!initialAvailability?.startTime.isNullOrBlank() && !initialAvailability?.endTime.isNullOrBlank()) {
                initialAvailability.startTime!!
            } else {
                defaultBetweenStart
            },
        )
    }
    var betweenEnd by remember(initialAvailability) {
        mutableStateOf(
            if (!initialAvailability?.startTime.isNullOrBlank() && !initialAvailability?.endTime.isNullOrBlank()) {
                initialAvailability.endTime!!
            } else {
                defaultBetweenEnd
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Beschikbaarheid aanpassen",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            TimeEditorOptionRow(
                label = "Hele dag beschikbaar",
                displayState = CalendarDayDisplayState.AVAILABLE_FULL,
                theme = theme,
                selected = selectedOption == TimeEditorOption.FULL_DAY,
                onClick = { selectedOption = TimeEditorOption.FULL_DAY },
            )
            TimeEditorOptionRow(
                label = "Niet beschikbaar",
                displayState = CalendarDayDisplayState.UNAVAILABLE,
                theme = theme,
                selected = selectedOption == TimeEditorOption.UNAVAILABLE,
                onClick = { selectedOption = TimeEditorOption.UNAVAILABLE },
            )
            TimeEditorOptionRow(
                label = "Misschien beschikbaar",
                displayState = CalendarDayDisplayState.MAYBE,
                theme = theme,
                selected = selectedOption == TimeEditorOption.MAYBE,
                onClick = { selectedOption = TimeEditorOption.MAYBE },
            )
            TimeEditorOptionRow(
                label = "Beschikbaar vanaf",
                displayState = CalendarDayDisplayState.AVAILABLE_FROM,
                theme = theme,
                selected = selectedOption == TimeEditorOption.FROM,
                onClick = { selectedOption = TimeEditorOption.FROM },
            )
            TimeEditorOptionRow(
                label = "Beschikbaar tot",
                displayState = CalendarDayDisplayState.AVAILABLE_UNTIL,
                theme = theme,
                selected = selectedOption == TimeEditorOption.UNTIL,
                onClick = { selectedOption = TimeEditorOption.UNTIL },
            )
            TimeEditorOptionRow(
                label = "Beschikbaar tussen",
                displayState = CalendarDayDisplayState.AVAILABLE_BETWEEN,
                theme = theme,
                selected = selectedOption == TimeEditorOption.BETWEEN,
                onClick = { selectedOption = TimeEditorOption.BETWEEN },
            )

            when (selectedOption) {
                TimeEditorOption.FROM -> {
                    CompactTimePicker(
                        label = "Vanaf",
                        time = fromTime,
                        onTimeChange = { fromTime = it },
                    )
                }

                TimeEditorOption.UNTIL -> {
                    CompactTimePicker(
                        label = "Tot",
                        time = untilTime,
                        onTimeChange = { untilTime = it },
                    )
                }

                TimeEditorOption.BETWEEN -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CompactTimePicker(
                            label = "Van",
                            time = betweenStart,
                            onTimeChange = { betweenStart = it },
                            modifier = Modifier.weight(1f),
                        )
                        CompactTimePicker(
                            label = "Tot",
                            time = betweenEnd,
                            onTimeChange = { betweenEnd = it },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                else -> Unit
            }

            Button(
                onClick = {
                    onApply(
                        availabilityFromSelection(
                            option = selectedOption,
                            fromTime = fromTime,
                            untilTime = untilTime,
                            betweenStart = betweenStart,
                            betweenEnd = betweenEnd,
                        ),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Toepassen",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun TimeEditorOptionRow(
    label: String,
    displayState: CalendarDayDisplayState,
    theme: AvailabilityCalendarTheme,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val style = theme.styleFor(displayState)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AccentOrange),
        )
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(style.backgroundColor),
        )
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactTimePicker(
    label: String,
    time: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TimeFieldBackground)
            .clickable { showDialog = true }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = time,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }

    if (showDialog) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showDialog = false },
            onConfirm = { selected ->
                onTimeChange(selected)
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val (hour, minute) = remember(initialTime) { parseTime(initialTime) }
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(formatTime(timePickerState.hour, timePickerState.minute))
                },
            ) {
                Text("OK", color = AccentOrange, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren", color = TextSecondary)
            }
        },
        title = {
            Text(
                text = "Tijd kiezen",
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

private fun optionFromAvailability(availability: DayAvailability?): TimeEditorOption {
    if (availability == null) return TimeEditorOption.FULL_DAY
    return when (availability.status) {
        DayAvailabilityStatus.UNAVAILABLE,
        DayAvailabilityStatus.NOT_SET,
        -> TimeEditorOption.UNAVAILABLE

        DayAvailabilityStatus.MAYBE -> TimeEditorOption.MAYBE
        DayAvailabilityStatus.AVAILABLE -> {
            val hasStart = !availability.startTime.isNullOrBlank()
            val hasEnd = !availability.endTime.isNullOrBlank()
            when {
                hasStart && hasEnd -> TimeEditorOption.BETWEEN
                hasStart -> TimeEditorOption.FROM
                hasEnd -> TimeEditorOption.UNTIL
                else -> TimeEditorOption.FULL_DAY
            }
        }
    }
}

private fun availabilityFromSelection(
    option: TimeEditorOption,
    fromTime: String,
    untilTime: String,
    betweenStart: String,
    betweenEnd: String,
): DayAvailability {
    return when (option) {
        TimeEditorOption.FULL_DAY -> DayAvailability.availableFullDay()
        TimeEditorOption.UNAVAILABLE -> DayAvailability.unavailable()
        TimeEditorOption.MAYBE -> DayAvailability.maybeFullDay()
        TimeEditorOption.FROM -> DayAvailability.availableFrom(fromTime)
        TimeEditorOption.UNTIL -> DayAvailability.availableUntil(untilTime)
        TimeEditorOption.BETWEEN -> DayAvailability.availableBetween(betweenStart, betweenEnd)
    }
}
