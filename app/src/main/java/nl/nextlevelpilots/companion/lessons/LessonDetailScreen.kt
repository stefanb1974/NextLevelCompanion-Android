package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign

@Composable
fun LessonDetailScreen(
    lessonId: String,
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val lesson = viewModel.lessonById(lessonId)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = CompanionDesign.ScreenPadding, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Terug",
                    tint = CompanionDesign.Navy,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = "Training",
                color = CompanionDesign.TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
        }

        when {
            uiState.isLoading && lesson == null -> {
                LessonDetailMessageCard(message = "Training laden…")
            }

            lesson == null -> {
                LessonDetailMessageCard(message = "Training niet gevonden.")
            }

            else -> {
                LessonDetailHeader(lesson = lesson)

                LessonDetailMainCard(
                    lesson = lesson,
                    confirmingLessonId = uiState.confirmingLessonId,
                    onConfirmLesson = viewModel::confirmLesson,
                )
            }
        }
    }
}

@Composable
private fun LessonDetailHeader(
    lesson: LessonUiModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = lesson.title ?: "Training",
            color = CompanionDesign.Navy,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        )
        LessonStatusBadge(
            label = lesson.statusLabel,
            status = lesson.status,
        )
    }
}

@Composable
private fun LessonDetailMainCard(
    lesson: LessonUiModel,
    confirmingLessonId: String?,
    onConfirmLesson: (String) -> Unit,
) {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LessonDetailLabelRow(
                label = "Datum",
                value = formatLessonDate(lesson.date),
            )

            LessonDetailLabelRow(
                label = "Tijd",
                value = lesson.timeRangeLabel,
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

            if (lesson.additionalStudentNames.isNotEmpty()) {
                LessonDetailLabelRow(
                    label = "Extra studenten",
                    value = lesson.additionalStudentNames.joinToString("\n"),
                )
            }

            if (lesson.additionalInstructorNames.isNotEmpty()) {
                LessonDetailLabelRow(
                    label = "Extra instructeurs",
                    value = lesson.additionalInstructorNames.joinToString("\n"),
                )
            }

            lesson.notes?.let { notes ->
                LessonDetailLabelRow(label = "Notities", value = notes)
            }

            LessonDetailConfirmationSection(
                lesson = lesson,
                isConfirming = lesson.apiId != null && lesson.apiId == confirmingLessonId,
                onConfirmLesson = onConfirmLesson,
            )
        }
    }
}

@Composable
private fun LessonDetailConfirmationSection(
    lesson: LessonUiModel,
    isConfirming: Boolean,
    onConfirmLesson: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Bevestiging",
            color = CompanionDesign.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.2.sp,
        )

        when {
            lesson.status == LessonStatus.CANCELLED -> {
                LessonCancelledBadge()
            }

            lesson.isUserConfirmed -> {
                LessonConfirmedBadge()
            }

            lesson.canConfirm && lesson.apiId != null -> {
                LessonConfirmButton(
                    onClick = { onConfirmLesson(lesson.apiId) },
                    isLoading = isConfirming,
                )
            }

            else -> {
                Text(
                    text = "Bevestiging niet beschikbaar",
                    color = CompanionDesign.TextSecondary,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun LessonDetailMessageCard(
    message: String,
) {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            color = CompanionDesign.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(CompanionDesign.CardPadding),
        )
    }
}
