package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LabelGrey = Color(0xFF9AA3BC)
private val ValueWhite = Color(0xFFE8EBF5)
private val AccentOrange = Color(0xFFFF8B56)

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
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp,
                )
            }
        }

        if (showDate) {
            Text(
                text = formatLessonDate(lesson.date),
                color = AccentOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            text = lesson.timeRangeLabel,
            color = if (showTitle || showDate) ValueWhite else AccentOrange,
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
            color = LabelGrey,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
        )
        Text(
            text = value,
            color = ValueWhite,
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
                color = LabelGrey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.2.sp,
            )
            when {
                lesson.isUserConfirmed -> {
                    LessonConfirmedBadge()
                }

                else -> {
                    LessonStatusBadge(
                        label = lesson.statusLabel,
                        status = lesson.status,
                    )
                }
            }
        }

        if (lesson.canConfirm && lesson.apiId != null && onConfirmLesson != null) {
            LessonConfirmButton(
                onClick = { onConfirmLesson(lesson.apiId) },
                isLoading = isConfirming,
            )
        }
    }
}
