package nl.nextlevelpilots.companion.trainingprogress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign
import nl.nextlevelpilots.companion.ui.PremiumPullRefresh

@Composable
fun TrainingProgressScreen(
    viewModel: TrainingProgressViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        TrainingProgressTopBar(onBack = onBack)

        PremiumPullRefresh(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshTrainingProgress,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                uiState.isLoading && uiState.courses.isEmpty() && !uiState.isRefreshing -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = CompanionDesign.Accent,
                            strokeWidth = 2.5.dp,
                        )
                    }
                }

                uiState.loadFailed && uiState.courses.isEmpty() -> {
                    TrainingProgressMessageCard(
                        message = uiState.errorMessage ?: TrainingProgressRepository.LOAD_ERROR_MESSAGE,
                    )
                }

                uiState.courses.isEmpty() -> {
                    TrainingProgressMessageCard(
                        message = "Geen actieve trainingen gevonden.",
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = CompanionDesign.ScreenPadding,
                            vertical = 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(CompanionDesign.ItemSpacing),
                    ) {
                        items(
                            items = uiState.courses,
                            key = { course -> course.id },
                        ) { course ->
                            TrainingProgressCourseCard(course = course)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrainingProgressTopBar(
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Terug",
                tint = CompanionDesign.Navy,
                modifier = Modifier.size(24.dp),
            )
        }
        Column {
            Text(
                text = "Trainingsvoortgang",
                color = CompanionDesign.Navy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Je ingeschreven trainingen",
                color = CompanionDesign.TextSecondary,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun TrainingProgressMessageCard(
    message: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CompanionDesign.ScreenPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompanionCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = message,
                color = CompanionDesign.TextPrimary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            )
        }
    }
}
