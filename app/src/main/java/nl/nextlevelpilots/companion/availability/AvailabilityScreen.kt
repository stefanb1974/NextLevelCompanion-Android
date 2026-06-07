package nl.nextlevelpilots.companion.availability

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import nl.nextlevelpilots.companion.availability.ui.AvailabilityCalendarLegend
import nl.nextlevelpilots.companion.availability.ui.CalendarInteractionMode
import nl.nextlevelpilots.companion.availability.ui.MultiDayActionPanel
import nl.nextlevelpilots.companion.availability.ui.AvailabilityCalendarTheme
import nl.nextlevelpilots.companion.availability.ui.AvailabilityCalendarUiMapper
import nl.nextlevelpilots.companion.availability.ui.AvailabilityMonthCalendar
import nl.nextlevelpilots.companion.availability.ui.AvailabilityScreenInteractionState
import nl.nextlevelpilots.companion.availability.ui.AvailabilityTimeEditorSheet
import nl.nextlevelpilots.companion.availability.ui.CalendarDayDisplayState
import nl.nextlevelpilots.companion.availability.ui.defaultDayStyles
import nl.nextlevelpilots.companion.availability.ui.handleCalendarDayClick
import nl.nextlevelpilots.companion.availability.ui.styleFor
import nl.nextlevelpilots.companion.availability.ui.withBulkEditEnabled
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val ScreenBackground = Color(0xFFF4F5F8)
private val CardWhite = Color.White
private val TextPrimary = Color(0xFF1C1C1E)
private val TextSecondary = Color(0xFF8E8E93)
private val NavButtonBackground = Color(0xFFE8EAEF)
private val AccentOrange = Color(0xFFFF7A3D)
private val AccentOrangeLight = Color(0xFFFFC090)
private val CalendarCardShape = RoundedCornerShape(28.dp)
private val SaveButtonShape = RoundedCornerShape(24.dp)

private val calendarTheme = AvailabilityCalendarTheme(
    dayStyles = defaultDayStyles(),
)

@Composable
fun AvailabilityScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel: AvailabilityViewModel = viewModel(
        factory = AvailabilityViewModel.factory(LocalContext.current),
    )
    val uiState by viewModel.uiState.collectAsState()
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }
    var bulkEditEnabled by rememberSaveable { mutableStateOf(false) }
    var interactionState by remember { mutableStateOf(AvailabilityScreenInteractionState()) }
    var timeEditorTarget by remember { mutableStateOf<TimeEditorTarget?>(null) }

    LaunchedEffect(bulkEditEnabled) {
        interactionState = interactionState.withBulkEditEnabled(bulkEditEnabled)
    }

    val calendarGridState = remember(
        uiState.currentMonth,
        uiState.days,
        uiState.savedDays,
        uiState.selectedDay,
        interactionState.multiSelectedDays,
    ) {
        AvailabilityCalendarUiMapper.mapGridForMonth(
            month = uiState.currentMonth,
            days = uiState.days,
            savedDays = uiState.savedDays,
            selectedDay = uiState.selectedDay,
            multiSelectedDays = interactionState.multiSelectedDays,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                AvailabilityMonthHeader(
                    month = uiState.currentMonth,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                )

                AvailabilityTopControls(
                    currentMonth = uiState.currentMonth,
                    onGoToToday = {
                        navigateToMonth(
                            current = uiState.currentMonth,
                            target = YearMonth.now(),
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth,
                        )
                    },
                    onInfoClick = { showInfoDialog = true },
                    bulkEditEnabled = bulkEditEnabled,
                    onBulkEditChanged = { bulkEditEnabled = it },
                )

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 64.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = AccentOrange,
                                strokeWidth = 2.5.dp,
                            )
                        }
                    }

                    uiState.loadFailed -> {
                        AvailabilityLoadErrorState(
                            message = uiState.loadErrorMessage
                                ?: AvailabilityRepository.LOAD_ERROR_MESSAGE,
                            onRetry = viewModel::loadAvailability,
                        )
                    }

                    else -> {
                        if (uiState.saveErrorMessage != null) {
                            AvailabilitySaveErrorBanner(
                                message = uiState.saveErrorMessage!!,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            AvailabilityMonthCalendar(
                                gridState = calendarGridState,
                                theme = calendarTheme,
                                interactionMode = interactionState.mode,
                                onDayClick = { date ->
                                    handleCalendarDayClick(
                                        date = date,
                                        interactionState = interactionState,
                                        onInteractionStateChange = { interactionState = it },
                                        onSingleDayTap = viewModel::onDayTapped,
                                    )
                                },
                                onDayLongClick = { date ->
                                    timeEditorTarget = TimeEditorTarget.Single(date)
                                },
                                onSwipePreviousMonth = viewModel::previousMonth,
                                onSwipeNextMonth = viewModel::nextMonth,
                            )

                            AvailabilityCalendarLegend(theme = calendarTheme)
                        }
                    }
                }
            }

            if (!uiState.loadFailed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ScreenBackground)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MultiDayActionPanel(
                        visible = interactionState.mode == CalendarInteractionMode.MULTI_DAY,
                        selectedCount = interactionState.multiSelectedDays.size,
                        theme = calendarTheme,
                        onApplyStatus = { status ->
                            viewModel.applyStatusToDates(
                                dates = interactionState.multiSelectedDays,
                                status = status,
                            )
                            interactionState = interactionState.copy(
                                multiSelectedDays = emptySet(),
                            )
                        },
                        onOpenTimeEditor = {
                            timeEditorTarget = TimeEditorTarget.Multiple(
                                dates = interactionState.multiSelectedDays,
                            )
                        },
                        onClearSelection = {
                            interactionState = interactionState.copy(
                                multiSelectedDays = emptySet(),
                            )
                        },
                    )

                    AvailabilitySaveSection(
                        hasPendingChanges = uiState.hasPendingChanges,
                        isSaving = uiState.isSaving,
                        isLoading = uiState.isLoading,
                        onSave = viewModel::saveChanges,
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        AvailabilityInfoDialog(onDismiss = { showInfoDialog = false })
    }

    timeEditorTarget?.let { target ->
        val initialAvailability = when (target) {
            is TimeEditorTarget.Single -> viewModel.availabilityFor(target.date)
            is TimeEditorTarget.Multiple -> null
        }
        AvailabilityTimeEditorSheet(
            initialAvailability = initialAvailability,
            theme = calendarTheme,
            onDismiss = { timeEditorTarget = null },
            onApply = { availability ->
                when (target) {
                    is TimeEditorTarget.Single -> {
                        viewModel.setDayAvailability(target.date, availability)
                    }

                    is TimeEditorTarget.Multiple -> {
                        viewModel.applyAvailabilityToDates(target.dates, availability)
                        interactionState = interactionState.copy(
                            multiSelectedDays = emptySet(),
                        )
                    }
                }
                timeEditorTarget = null
            },
        )
    }
}

private sealed class TimeEditorTarget {
    data class Single(val date: LocalDate) : TimeEditorTarget()
    data class Multiple(val dates: Set<LocalDate>) : TimeEditorTarget()
}

@Composable
private fun AvailabilityMonthHeader(
    month: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    val dutchLocale = remember { Locale.forLanguageTag("nl-NL") }
    val monthTitle = remember(month, dutchLocale) {
        month.atDay(1)
            .format(DateTimeFormatter.ofPattern("MMMM yyyy", dutchLocale))
            .replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(dutchLocale) else char.toString()
            }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        MonthNavButton(
            onClick = onPreviousMonth,
            contentDescription = "Vorige maand",
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = monthTitle,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )

        MonthNavButton(
            onClick = onNextMonth,
            contentDescription = "Volgende maand",
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun AvailabilityTopControls(
    currentMonth: YearMonth,
    onGoToToday: () -> Unit,
    onInfoClick: () -> Unit,
    bulkEditEnabled: Boolean,
    onBulkEditChanged: (Boolean) -> Unit,
) {
    val isCurrentMonth = currentMonth == YearMonth.now()
    val controlShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onGoToToday,
            enabled = !isCurrentMonth,
            shape = controlShape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = TextPrimary,
                disabledContentColor = TextSecondary.copy(alpha = 0.6f),
            ),
            modifier = Modifier
                .height(40.dp)
                .background(
                    color = if (isCurrentMonth) Color(0xFFEDEFF3) else NavButtonBackground,
                    shape = controlShape,
                ),
        ) {
            Text(
                text = "Vandaag",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NavButtonBackground),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Informatie",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier.height(40.dp),
            shape = controlShape,
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Meerdere dagen",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = bulkEditEnabled,
                    onCheckedChange = onBulkEditChanged,
                    modifier = Modifier.scale(0.82f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentOrange,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD1D5DE),
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AvailabilityLoadErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = CalendarCardShape,
                ambientColor = Color.Black.copy(alpha = 0.05f),
                spotColor = Color.Black.copy(alpha = 0.07f),
            ),
        shape = CalendarCardShape,
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = message,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Opnieuw proberen",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text = "Controleer je verbinding en probeer het opnieuw.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun AvailabilitySaveErrorBanner(
    message: String,
) {
    val unavailableStyle = calendarTheme.styleFor(CalendarDayDisplayState.UNAVAILABLE)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = unavailableStyle.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = message,
            color = unavailableStyle.textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun AvailabilitySaveSection(
    hasPendingChanges: Boolean,
    isSaving: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit,
) {
    val saveEnabled = hasPendingChanges && !isSaving && !isLoading
    val gradientBrush = Brush.linearGradient(
        colors = listOf(AccentOrange, AccentOrangeLight),
    )
    val disabledBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFD4D8E0),
            Color(0xFFE2E5EB),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(
                    elevation = if (saveEnabled) 10.dp else 3.dp,
                    shape = SaveButtonShape,
                    ambientColor = AccentOrange.copy(alpha = if (saveEnabled) 0.12f else 0.04f),
                    spotColor = AccentOrange.copy(alpha = if (saveEnabled) 0.16f else 0.06f),
                )
                .clip(SaveButtonShape)
                .background(if (saveEnabled) gradientBrush else disabledBrush)
                .clickable(
                    enabled = saveEnabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onSave,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = if (saveEnabled) 1f else 0.7f),
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = if (hasPendingChanges) {
                            "Alle wijzigingen opslaan"
                        } else {
                            "Geen wijzigingen"
                        },
                        color = Color.White.copy(alpha = if (saveEnabled) 1f else 0.7f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Text(
            text = "NextLevel Pilots",
            color = TextSecondary.copy(alpha = 0.85f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun MonthNavButton(
    onClick: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(NavButtonBackground),
    ) {
        content()
    }
}

@Composable
private fun AvailabilityInfoDialog(
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = CardWhite,
        title = {
            Text(
                text = "Beschikbaarheid",
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tik op een dag om je beschikbaarheid aan te passen.",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                )
                InfoColorRow(
                    displayState = CalendarDayDisplayState.AVAILABLE_FULL,
                    label = "Beschikbaar",
                )
                InfoColorRow(
                    displayState = CalendarDayDisplayState.UNAVAILABLE,
                    label = "Niet beschikbaar",
                )
                InfoColorRow(
                    displayState = CalendarDayDisplayState.MAYBE,
                    label = "Misschien beschikbaar",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Sluiten",
                    color = AccentOrange,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
    )
}

@Composable
private fun InfoColorRow(
    displayState: CalendarDayDisplayState,
    label: String,
) {
    val style = calendarTheme.styleFor(displayState)

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(style.backgroundColor),
        )
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun navigateToMonth(
    current: YearMonth,
    target: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val currentOrdinal = current.year * 12 + current.monthValue
    val targetOrdinal = target.year * 12 + target.monthValue
    val diff = targetOrdinal - currentOrdinal
    repeat(abs(diff)) {
        if (diff > 0) onNext() else onPrevious()
    }
}
