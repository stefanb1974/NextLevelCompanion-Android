package nl.nextlevelpilots.companion.trainingprogress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign

@Composable
fun TrainingProgressCourseCard(
    course: TrainingProgressCourseUiModel,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    CompanionCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
        ) {
            Text(
                text = course.courseName,
                color = CompanionDesign.Navy,
                fontSize = if (compact) 15.sp else 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            TrainingProgressBar(
                progressPercent = course.progressPercent,
                compact = compact,
            )

            course.nextLessonTitle?.let { title ->
                TrainingProgressNextLessonRow(
                    title = title,
                    dateLabel = course.nextLessonDateLabel,
                    timeLabel = course.nextLessonTimeLabel,
                    compact = compact,
                )
            }

            if (!compact) {
                course.latestFeedback?.let { feedback ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Laatste feedback",
                            color = CompanionDesign.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = feedback,
                            color = CompanionDesign.TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingProgressBar(
    progressPercent: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Voortgang",
                color = CompanionDesign.TextSecondary,
                fontSize = if (compact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "$progressPercent%",
                color = CompanionDesign.Accent,
                fontSize = if (compact) 13.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        LinearProgressIndicator(
            progress = { progressPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = CompanionDesign.Accent,
            trackColor = CompanionDesign.Border,
        )
    }
}

@Composable
private fun TrainingProgressNextLessonRow(
    title: String,
    dateLabel: String?,
    timeLabel: String?,
    compact: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Volgende les",
            color = CompanionDesign.TextSecondary,
            fontSize = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = title,
            color = CompanionDesign.Navy,
            fontSize = if (compact) 14.sp else 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        val schedule = listOfNotNull(dateLabel, timeLabel).joinToString(" · ")
        if (schedule.isNotBlank()) {
            Text(
                text = schedule,
                color = CompanionDesign.TextTertiary,
                fontSize = if (compact) 12.sp else 13.sp,
            )
        }
    }
}
