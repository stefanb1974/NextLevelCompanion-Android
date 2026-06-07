package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.lessons.LessonConfirmButton
import nl.nextlevelpilots.companion.lessons.LessonConfirmedBadge
import nl.nextlevelpilots.companion.lessons.LessonUiModel
import nl.nextlevelpilots.companion.lessons.LessonsUiState
import nl.nextlevelpilots.companion.lessons.LessonsViewModel
import nl.nextlevelpilots.companion.lessons.formatRelativeLessonDate
import nl.nextlevelpilots.companion.lessons.isInstructorRole
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign

@Composable
fun NextTrainingHeroCard(
    lessonsState: LessonsUiState,
    userRole: String?,
    lessonsViewModel: LessonsViewModel,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CompanionCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Volgende training",
                color = CompanionDesign.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )

            when {
                lessonsState.isLoading && lessonsState.nextUpcomingLesson == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 20.dp),
                        color = CompanionDesign.Accent,
                        strokeWidth = 2.5.dp,
                    )
                }

                lessonsState.nextUpcomingLesson != null -> {
                    NextTrainingCardContent(
                        lesson = lessonsState.nextUpcomingLesson!!,
                        userRole = userRole,
                        confirmingLessonId = lessonsState.confirmingLessonId,
                        onConfirmLesson = lessonsViewModel::confirmLesson,
                        onClick = { onLessonClick(lessonsState.nextUpcomingLesson!!.id) },
                    )
                }

                else -> NextTrainingEmptyState()
            }
        }
    }
}

@Composable
private fun NextTrainingCardContent(
    lesson: LessonUiModel,
    userRole: String?,
    confirmingLessonId: String?,
    onConfirmLesson: (String) -> Unit,
    onClick: () -> Unit,
) {
    val participantName = lesson.participantLabelForViewer(isInstructorRole(userRole))
    val pillShape = RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = formatRelativeLessonDate(lesson.date),
            color = CompanionDesign.Accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(CompanionDesign.Accent.copy(alpha = 0.12f), pillShape)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )

        lesson.title?.let { title ->
            Text(
                text = title,
                color = CompanionDesign.Navy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
            )
        }

        lesson.courseName?.let { course ->
            Text(text = course, color = CompanionDesign.TextSecondary, fontSize = 15.sp)
        }

        Text(
            text = lesson.timeRangeLabel,
            color = CompanionDesign.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )

        lesson.location?.let {
            Text(text = it, color = CompanionDesign.TextTertiary, fontSize = 14.sp)
        }

        participantName?.let {
            Text(text = it, color = CompanionDesign.TextTertiary, fontSize = 14.sp)
        }

        when {
            lesson.canConfirm && lesson.apiId != null -> {
                LessonConfirmButton(
                    onClick = { onConfirmLesson(lesson.apiId) },
                    isLoading = lesson.apiId == confirmingLessonId,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            lesson.isConfirmed -> LessonConfirmedBadge()
        }
    }
}

@Composable
fun NextTrainingEmptyState(modifier: Modifier = Modifier) {
    Text(
        text = "Geen geplande training",
        color = CompanionDesign.TextSecondary,
        fontSize = 16.sp,
        modifier = modifier,
    )
}
