package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.lessons.LessonDetailFields
import nl.nextlevelpilots.companion.lessons.LessonsViewModel

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
    val mutedGrey = Color(0xFF9AA3BC)
    val glassBackground = Color.White.copy(alpha = 0.06f)
    val glassBorder = Color.White.copy(alpha = 0.12f)
    val cardShape = RoundedCornerShape(28.dp)
    val accentOrange = Color(0xFFFF8B56)

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
                    lessonsState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 8.dp),
                            color = accentOrange,
                            strokeWidth = 2.dp,
                        )
                    }

                    lessonsState.nextUpcomingLesson != null -> {
                        val lesson = lessonsState.nextUpcomingLesson!!
                        LessonDetailFields(
                            lesson = lesson,
                            showDate = true,
                            confirmingLessonId = lessonsState.confirmingLessonId,
                            onConfirmLesson = lessonsViewModel::confirmLesson,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onLessonClick(lesson.id) },
                            ),
                        )
                    }

                    else -> {
                        Text(
                            text = "Geen geplande training",
                            color = lightGrey,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                        )
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
                    text = "Gebruik de navigatie onderaan om trainingen, beschikbaarheid en je profiel te bekijken.",
                    color = lightGrey,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}
