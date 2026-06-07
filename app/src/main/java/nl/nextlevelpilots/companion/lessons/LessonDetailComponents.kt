package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import nl.nextlevelpilots.companion.ui.CompanionDesign

@Composable
fun LessonDetailFields(
    lesson: LessonUiModel,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
    showDate: Boolean = false,
    confirmingLessonId: String? = null,
    onConfirmLesson: ((String) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showTitle) {
            lesson.title?.let { title ->
                Text(
                    text = title,
                    color = CompanionDesign.Navy,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp,
                )
            }
        }

        if (showDate) {
            Text(
                text = formatLessonDate(lesson.date),
                color = CompanionDesign.Accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = lesson.timeRangeLabel,
            color = if (showTitle || showDate) CompanionDesign.TextPrimary else CompanionDesign.Accent,
            fontSize = if (showTitle || showDate) 16.sp else 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        lesson.location?.let { location ->
            LessonDetailLabelRow(label = "Locatie", value = location)
        }

        lesson.courseName?.let { course ->
            LessonDetailLabelRow(label = "Cursus", value = course)
        }

        lesson.traineeName?.let { trainee ->
            LessonDetailLabelRow(label = "Student", value = trainee)
        }

        lesson.instructorName?.let { instructor ->
            LessonDetailLabelRow(label = "Instructeur", value = instructor)
        }

        LessonDetailStatusSection(
            lesson = lesson,
            isConfirming = lesson.apiId != null && lesson.apiId == confirmingLessonId,
            onConfirmLesson = onConfirmLesson,
        )
    }
}

@Composable
fun LessonDetailLabelRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            color = CompanionDesign.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
        )
        Text(
            text = value,
            color = CompanionDesign.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp,
        )
    }
}

@Composable
private fun LessonDetailStatusSection(
    lesson: LessonUiModel,
    isConfirming: Boolean,
    onConfirmLesson: ((String) -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Status",
                color = CompanionDesign.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp,
            )
            LessonStatusBadge(
                label = lesson.statusLabel,
                status = lesson.status,
            )
        }

        when {
            lesson.canConfirm && lesson.apiId != null && onConfirmLesson != null -> {
                LessonConfirmButton(
                    onClick = { onConfirmLesson(lesson.apiId) },
                    isLoading = isConfirming,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            lesson.isConfirmed -> LessonConfirmedBadge()
        }
    }
}
