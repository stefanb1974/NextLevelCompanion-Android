package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.trainingprogress.TrainingProgressBar
import nl.nextlevelpilots.companion.trainingprogress.TrainingProgressCourseUiModel
import nl.nextlevelpilots.companion.trainingprogress.TrainingProgressUiState
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign

private const val DASHBOARD_COURSE_LIMIT = 2

@Composable
fun DashboardTrainingProgressCard(
    state: TrainingProgressUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompanionCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier.padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Trainingsvoortgang",
                    color = CompanionDesign.Navy,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CompanionDesign.TextTertiary,
                    modifier = Modifier.size(22.dp),
                )
            }

            when {
                state.isLoading && state.courses.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = CompanionDesign.Accent,
                            strokeWidth = 2.5.dp,
                        )
                    }
                }

                state.loadFailed && state.courses.isEmpty() -> {
                    Text(
                        text = state.errorMessage ?: "Trainingsvoortgang niet beschikbaar",
                        color = CompanionDesign.TextSecondary,
                        fontSize = 14.sp,
                    )
                }

                state.courses.isEmpty() -> {
                    Text(
                        text = "Geen actieve trainingen",
                        color = CompanionDesign.TextSecondary,
                        fontSize = 14.sp,
                    )
                }

                else -> {
                    state.courses.take(DASHBOARD_COURSE_LIMIT).forEach { course ->
                        DashboardCourseSummary(course = course)
                    }

                    if (state.courses.size > DASHBOARD_COURSE_LIMIT) {
                        Text(
                            text = "+${state.courses.size - DASHBOARD_COURSE_LIMIT} meer",
                            color = CompanionDesign.Accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCourseSummary(
    course: TrainingProgressCourseUiModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = course.courseName,
            color = CompanionDesign.Navy,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )

        TrainingProgressBar(
            progressPercent = course.progressPercent,
            compact = true,
        )

        course.nextLessonTitle?.let { title ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Volgende les",
                    color = CompanionDesign.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = title,
                    color = CompanionDesign.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
