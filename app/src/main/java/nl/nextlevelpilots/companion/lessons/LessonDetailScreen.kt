package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightGrey = Color(0xFFB8BCD4)
private val GlassBackground = Color.White.copy(alpha = 0.08f)
private val GlassBorder = Color.White.copy(alpha = 0.14f)
private val AccentOrange = Color(0xFFFF8B56)
private val CardShape = RoundedCornerShape(28.dp)

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
            .padding(horizontal = 24.dp, vertical = 24.dp),
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
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = "Training",
                color = LightGrey,
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
                LessonDetailHeader(
                    lesson = lesson,
                )

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
            color = Color.White,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = CardShape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.2f),
            )
            .border(1.dp, GlassBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 26.dp),
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
            color = Color(0xFF9AA3BC),
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
                    color = LightGrey,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(28.dp),
        )
    }
}
