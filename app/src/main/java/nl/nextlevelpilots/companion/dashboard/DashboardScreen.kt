package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.lessons.LessonsViewModel
import nl.nextlevelpilots.companion.ui.PremiumPullRefresh

@Composable
fun DashboardScreen(
    userName: String?,
    userEmail: String?,
    userRole: String?,
    lessonsViewModel: LessonsViewModel,
    onLessonClick: (String) -> Unit = {},
) {
    val lessonsState by lessonsViewModel.uiState.collectAsState()
    val lightGrey = Color(0xFFB8BCD4)
    val glassBackground = Color.White.copy(alpha = 0.06f)
    val glassBorder = Color.White.copy(alpha = 0.12f)
    val cardShape = RoundedCornerShape(28.dp)
    val accentOrange = Color(0xFFFF8B56)

    PremiumPullRefresh(
        isRefreshing = lessonsState.isRefreshing,
        onRefresh = lessonsViewModel::refreshLessons,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "NextLevel Pilots",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
            )

            Text(
                text = "COMPANION",
                color = lightGrey,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, glassBorder, cardShape),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = glassBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                ) {
                    Text(
                        text = if (!userName.isNullOrBlank()) {
                            "Welkom $userName"
                        } else {
                            "Welkom"
                        },
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!userEmail.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = userEmail,
                            color = lightGrey,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, glassBorder, cardShape),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = glassBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Volgende training",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    when {
                        lessonsState.isLoading && lessonsState.nextUpcomingLesson == null -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 8.dp),
                                color = accentOrange,
                                strokeWidth = 2.dp,
                            )
                        }

                        lessonsState.nextUpcomingLesson != null -> {
                            NextTrainingCard(
                                lesson = lessonsState.nextUpcomingLesson!!,
                                userRole = userRole,
                                confirmingLessonId = lessonsState.confirmingLessonId,
                                onConfirmLesson = lessonsViewModel::confirmLesson,
                                onClick = { onLessonClick(lessonsState.nextUpcomingLesson!!.id) },
                            )
                        }

                        else -> {
                            NextTrainingEmptyState()
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, glassBorder, cardShape),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = glassBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Je overzicht",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Gebruik de navigatie onderaan om trainingen, documenten, beschikbaarheid en je profiel te bekijken.",
                        color = lightGrey,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    )
                }
            }
        }
    }
}
