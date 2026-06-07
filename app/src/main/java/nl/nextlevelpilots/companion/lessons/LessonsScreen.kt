package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightGrey = Color(0xFFB8BCD4)
private val GlassBackground = Color.White.copy(alpha = 0.06f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val AccentOrange = Color(0xFFFF8B56)
private val CardShape = RoundedCornerShape(28.dp)
private val LessonCardShape = RoundedCornerShape(22.dp)

@Composable
fun LessonsScreen(
    viewModel: LessonsViewModel,
    userRole: String? = null,
    onLessonClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Trainingen",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Je geplande lessen en trainingen",
            color = LightGrey,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = AccentOrange,
                        strokeWidth = 2.5.dp,
                    )
                }
            }

            uiState.loadFailed -> {
                LessonsErrorCard(
                    message = uiState.errorMessage ?: LessonsRepository.LOAD_ERROR_MESSAGE,
                    onRetry = viewModel::loadLessons,
                )
            }

            uiState.lessonsByDate.isEmpty() -> {
                LessonsEmptyCard()
            }

            else -> {
                uiState.lessonsByDate.forEach { group ->
                    LessonDateSection(
                        group = group,
                        confirmingLessonId = uiState.confirmingLessonId,
                        onConfirmLesson = viewModel::confirmLesson,
                        onLessonClick = onLessonClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonDateSection(
    group: LessonDateGroup,
    confirmingLessonId: String?,
    onConfirmLesson: (String) -> Unit,
    onLessonClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = group.dateLabel,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
        )

        group.lessons.forEach { lesson ->
            LessonCard(
                lesson = lesson,
                confirmingLessonId = confirmingLessonId,
                onConfirmLesson = onConfirmLesson,
                onLessonClick = onLessonClick,
            )
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonUiModel,
    confirmingLessonId: String?,
    onConfirmLesson: (String) -> Unit,
    onLessonClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onLessonClick(lesson.id) },
            )
            .border(1.dp, GlassBorder, LessonCardShape),
        shape = LessonCardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        LessonDetailFields(
            lesson = lesson,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            confirmingLessonId = confirmingLessonId,
            onConfirmLesson = onConfirmLesson,
        )
    }
}

@Composable
private fun LessonsEmptyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Geen geplande trainingen",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Er staan geen trainingen in deze periode gepland.",
                color = LightGrey,
                fontSize = 14.sp,
                lineHeight = 20.sp,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                color = Color.White,
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
                shape = RoundedCornerShape(18.dp),
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
        }
    }
}
