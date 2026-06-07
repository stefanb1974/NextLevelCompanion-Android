package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.lessons.LessonsViewModel
import nl.nextlevelpilots.companion.navigation.MainTab
import nl.nextlevelpilots.companion.trainingprogress.TrainingProgressViewModel
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign
import nl.nextlevelpilots.companion.ui.PremiumPullRefresh

@Composable
fun DashboardScreen(
    userName: String?,
    userEmail: String?,
    userRole: String?,
    lessonsViewModel: LessonsViewModel,
    trainingProgressViewModel: TrainingProgressViewModel,
    onLessonClick: (String) -> Unit = {},
    onQuickAction: (MainTab) -> Unit = {},
    onTrainingProgressClick: () -> Unit = {},
) {
    val lessonsState by lessonsViewModel.uiState.collectAsState()
    val trainingProgressState by trainingProgressViewModel.uiState.collectAsState()
    val firstName = firstNameFrom(userName)
    val greeting = if (firstName != null) "Hoi, $firstName" else "Hoi"

    PremiumPullRefresh(
        isRefreshing = lessonsState.isRefreshing,
        onRefresh = lessonsViewModel::refreshLessons,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = CompanionDesign.ScreenPadding,
                vertical = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(CompanionDesign.SectionSpacing),
        ) {
            item {
                DashboardGreetingCard(
                    greeting = greeting,
                    subtitle = userEmail,
                )
            }

            item {
                NextTrainingHeroCard(
                    lessonsState = lessonsState,
                    userRole = userRole,
                    lessonsViewModel = lessonsViewModel,
                    onLessonClick = onLessonClick,
                )
            }

            item {
                DashboardQuickActions(onAction = onQuickAction)
            }

            item {
                DashboardTrainingProgressCard(
                    state = trainingProgressState,
                    onClick = onTrainingProgressClick,
                )
            }
        }
    }
}

@Composable
private fun DashboardGreetingCard(
    greeting: String,
    subtitle: String?,
) {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = greeting,
                color = CompanionDesign.Navy,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = CompanionDesign.TextSecondary,
                    fontSize = 15.sp,
                )
            }
        }
    }
}
