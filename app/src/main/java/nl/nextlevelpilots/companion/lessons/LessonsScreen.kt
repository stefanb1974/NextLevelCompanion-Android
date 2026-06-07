package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign
import nl.nextlevelpilots.companion.ui.PremiumPullRefresh

@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel,
    userRole: String? = null,
    onLessonClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    PremiumPullRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refreshLessons,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = CompanionDesign.ScreenPadding,
                vertical = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(CompanionDesign.ItemSpacing),
        ) {
            item {
                Text(
                    text = "Trainingen",
                    color = CompanionDesign.Navy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            when {
                uiState.isLoading && uiState.lessonsByDate.isEmpty() && !uiState.loadFailed -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = CompanionDesign.Accent,
                                strokeWidth = 2.5.dp,
                            )
                        }
                    }
                }

                uiState.loadFailed -> {
                    item {
                        LessonsErrorCard(
                            message = uiState.errorMessage ?: LessonsRepository.LOAD_ERROR_MESSAGE,
                            onRetry = viewModel::loadLessons,
                        )
                    }
                }

                uiState.lessonsByDate.isEmpty() -> {
                    item { LessonsEmptyCard() }
                }

                else -> {
                    uiState.lessonsByDate.forEach { group ->
                        item(key = "header-${group.date}") {
                            Text(
                                text = group.dateLabel,
                                color = CompanionDesign.TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                            )
                        }

                        items(
                            items = group.lessons,
                            key = { lesson -> lesson.id },
                        ) { lesson ->
                            LessonCard(
                                lesson = lesson,
                                userRole = userRole,
                                confirmingLessonId = uiState.confirmingLessonId,
                                onConfirmLesson = viewModel::confirmLesson,
                                onLessonClick = onLessonClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonUiModel,
    userRole: String?,
    confirmingLessonId: String?,
    onConfirmLesson: (String) -> Unit,
    onLessonClick: (String) -> Unit,
) {
    val participantLabel = lesson.participantLabelForViewer(isInstructorRole(userRole))

    CompanionCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onLessonClick(lesson.id) },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CompanionDesign.CardPadding),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            LessonDateBadge(date = lesson.date)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = lesson.title ?: "Training",
                        color = CompanionDesign.Navy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                    )
                    LessonStatusBadge(
                        label = lesson.statusLabel,
                        status = lesson.status,
                    )
                }

                lesson.courseName?.let { course ->
                    Text(
                        text = course,
                        color = CompanionDesign.TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Text(
                    text = lesson.timeRangeLabel,
                    color = CompanionDesign.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )

                lesson.location?.let { location ->
                    Text(
                        text = location,
                        color = CompanionDesign.TextTertiary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                participantLabel?.let { participant ->
                    Text(
                        text = participant,
                        color = CompanionDesign.TextTertiary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                when {
                    lesson.canConfirm && lesson.apiId != null -> {
                        LessonConfirmButton(
                            onClick = { onConfirmLesson(lesson.apiId) },
                            isLoading = lesson.apiId == confirmingLessonId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                        )
                    }

                    lesson.isConfirmed -> {
                        LessonConfirmedBadge(modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonsEmptyCard() {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Geen geplande trainingen",
                color = CompanionDesign.Navy,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Er staan geen trainingen in deze periode gepland.",
                color = CompanionDesign.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LessonsErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                color = CompanionDesign.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CompanionDesign.ButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CompanionDesign.Accent,
                    contentColor = CompanionDesign.CardWhite,
                ),
            ) {
                Text(
                    text = "Opnieuw proberen",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
