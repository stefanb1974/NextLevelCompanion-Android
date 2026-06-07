package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.lessons.LessonConfirmButton
import nl.nextlevelpilots.companion.lessons.LessonConfirmedBadge
import nl.nextlevelpilots.companion.lessons.LessonUiModel
import nl.nextlevelpilots.companion.lessons.formatRelativeLessonDate
import nl.nextlevelpilots.companion.lessons.isInstructorRole

private val LightGrey = Color(0xFFB8BCD4)
private val MutedGrey = Color(0xFF9AA3BC)
private val AccentOrange = Color(0xFFFF8B56)

@Composable
fun NextTrainingCard(
    lesson: LessonUiModel,
    userRole: String?,
    confirmingLessonId: String?,
    onConfirmLesson: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInstructor = isInstructorRole(userRole)
    val participantName = lesson.participantLabelForViewer(isInstructor)

    Column(
        modifier = modifier
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
            color = AccentOrange,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        lesson.title?.let { title ->
            Text(
                text = title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
            )
        }

        lesson.courseName?.let { course ->
            Text(
                text = course,
                color = LightGrey,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
            )
        }

        Text(
            text = lesson.timeRangeLabel,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        lesson.location?.let { location ->
            Text(
                text = location,
                color = MutedGrey,
                fontSize = 14.sp,
                lineHeight = 19.sp,
            )
        }

        participantName?.let { participant ->
            Text(
                text = participant,
                color = MutedGrey,
                fontSize = 14.sp,
                lineHeight = 19.sp,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                lesson.canConfirm && lesson.apiId != null -> {
                    LessonConfirmButton(
                        onClick = { onConfirmLesson(lesson.apiId) },
                        isLoading = lesson.apiId == confirmingLessonId,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                lesson.isConfirmed -> {
                    LessonConfirmedBadge()
                }
            }
        }
    }
}

@Composable
fun NextTrainingEmptyState(
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Geen geplande training",
        color = LightGrey,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        modifier = modifier,
    )
}
