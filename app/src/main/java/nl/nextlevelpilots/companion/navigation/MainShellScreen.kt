package nl.nextlevelpilots.companion.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.nextlevelpilots.companion.availability.AvailabilityScreen
import nl.nextlevelpilots.companion.dashboard.DashboardScreen
import nl.nextlevelpilots.companion.dashboard.TabPlaceholderContent
import nl.nextlevelpilots.companion.lessons.LessonDetailScreen
import nl.nextlevelpilots.companion.lessons.LessonsScreen
import nl.nextlevelpilots.companion.lessons.LessonsViewModel

@Composable
fun MainShellScreen(
    userName: String?,
    userEmail: String?,
    userRole: String?,
    onLogout: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val shellNavController = rememberNavController()
    val lessonsViewModel: LessonsViewModel = viewModel(
        factory = LessonsViewModel.factory(LocalContext.current),
    )
    val lessonsState by lessonsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by shellNavController.currentBackStackEntryAsState()
    val showBottomNav = navBackStackEntry?.destination?.route == ShellRoutes.MAIN
    val gradientTop = Color(0xFF22287A)
    val gradientBottom = Color(0xFF3439A8)
    val accentOrange = Color(0xFFFF8B56)

    LaunchedEffect(lessonsState.snackbarMessage) {
        val message = lessonsState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        lessonsViewModel.clearSnackbar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom),
                ),
            ),
    ) {
        NavHost(
            navController = shellNavController,
            startDestination = ShellRoutes.MAIN,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomNav) 88.dp else 0.dp),
        ) {
            composable(ShellRoutes.MAIN) {
                when (selectedTab) {
                    MainTab.HOME -> DashboardScreen(
                        userName = userName,
                        userEmail = userEmail,
                        userRole = userRole,
                        lessonsViewModel = lessonsViewModel,
                        onLessonClick = { lessonId ->
                            shellNavController.navigate(ShellRoutes.lessonDetail(lessonId))
                        },
                    )

                    MainTab.TRAINING -> LessonsScreen(
                        viewModel = lessonsViewModel,
                        userRole = userRole,
                        onLessonClick = { lessonId ->
                            shellNavController.navigate(ShellRoutes.lessonDetail(lessonId))
                        },
                    )

                    MainTab.AVAILABILITY -> AvailabilityScreen()

                    MainTab.PROFILE -> TabPlaceholderContent(
                        title = "Profiel",
                        footer = {
                            Button(
                                onClick = onLogout,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentOrange,
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text(
                                    text = "UITLOGGEN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        },
                    )
                }
            }

            composable(
                route = ShellRoutes.LESSON_DETAIL,
                arguments = listOf(
                    navArgument("lessonId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId").orEmpty()
                LessonDetailScreen(
                    lessonId = lessonId,
                    viewModel = lessonsViewModel,
                    onBack = { shellNavController.popBackStack() },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (showBottomNav) 96.dp else 16.dp)
                .padding(horizontal = 16.dp),
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF12153A).copy(alpha = 0.95f),
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
            )
        }

        if (showBottomNav) {
            PremiumBottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
