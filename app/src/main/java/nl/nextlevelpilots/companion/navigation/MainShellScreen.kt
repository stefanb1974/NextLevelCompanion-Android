package nl.nextlevelpilots.companion.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import nl.nextlevelpilots.companion.availability.AvailabilityScreen
import nl.nextlevelpilots.companion.dashboard.DashboardScreen
import nl.nextlevelpilots.companion.documents.DocumentPdfViewerScreen
import nl.nextlevelpilots.companion.documents.DocumentsScreen
import nl.nextlevelpilots.companion.documents.DocumentsViewModel
import nl.nextlevelpilots.companion.lessons.LessonDetailScreen
import nl.nextlevelpilots.companion.lessons.LessonsScreen
import nl.nextlevelpilots.companion.lessons.LessonsViewModel
import nl.nextlevelpilots.companion.profile.ProfileScreen
import nl.nextlevelpilots.companion.trainingprogress.TrainingProgressScreen
import nl.nextlevelpilots.companion.trainingprogress.TrainingProgressViewModel
import nl.nextlevelpilots.companion.ui.CompanionDesign

@Composable
fun MainShellScreen(
    userName: String?,
    userEmail: String?,
    userRole: String?,
    linkedPersonId: String?,
    onLogout: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val shellNavController = rememberNavController()
    val lessonsViewModel: LessonsViewModel = viewModel(
        factory = LessonsViewModel.factory(LocalContext.current),
    )
    val documentsViewModel: DocumentsViewModel = viewModel(
        factory = DocumentsViewModel.factory(LocalContext.current),
    )
    val trainingProgressViewModel: TrainingProgressViewModel = viewModel(
        factory = TrainingProgressViewModel.factory(LocalContext.current),
    )
    val lessonsState by lessonsViewModel.uiState.collectAsState()
    val documentsState by documentsViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by shellNavController.currentBackStackEntryAsState()
    val showBottomNav = navBackStackEntry?.destination?.route == ShellRoutes.MAIN

    LaunchedEffect(lessonsState.snackbarMessage) {
        val message = lessonsState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        lessonsViewModel.clearSnackbar()
    }

    LaunchedEffect(documentsState.snackbarMessage) {
        val message = documentsState.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        documentsViewModel.clearSnackbar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CompanionDesign.Background),
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
                        trainingProgressViewModel = trainingProgressViewModel,
                        onLessonClick = { lessonId ->
                            shellNavController.navigate(ShellRoutes.lessonDetail(lessonId))
                        },
                        onQuickAction = { tab -> selectedTab = tab },
                        onTrainingProgressClick = {
                            shellNavController.navigate(ShellRoutes.TRAINING_PROGRESS)
                        },
                    )

                    MainTab.TRAINING -> LessonsScreen(
                        viewModel = lessonsViewModel,
                        userRole = userRole,
                        onLessonClick = { lessonId ->
                            shellNavController.navigate(ShellRoutes.lessonDetail(lessonId))
                        },
                    )

                    MainTab.DOCUMENTS -> DocumentsScreen(
                        viewModel = documentsViewModel,
                        onDocumentClick = { documentId ->
                            shellNavController.navigate(ShellRoutes.documentPdfViewer(documentId))
                        },
                    )

                    MainTab.AVAILABILITY -> AvailabilityScreen()

                    MainTab.PROFILE -> ProfileScreen(
                        userName = userName,
                        userEmail = userEmail,
                        userRole = userRole,
                        linkedPersonId = linkedPersonId,
                        onLogout = onLogout,
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

            composable(ShellRoutes.TRAINING_PROGRESS) {
                TrainingProgressScreen(
                    viewModel = trainingProgressViewModel,
                    onBack = { shellNavController.popBackStack() },
                )
            }

            composable(
                route = ShellRoutes.DOCUMENT_PDF_VIEWER,
                arguments = listOf(
                    navArgument("documentId") { type = NavType.StringType },
                ),
            ) { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
                DocumentPdfViewerScreen(
                    documentId = documentId,
                    viewModel = documentsViewModel,
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
                containerColor = CompanionDesign.Navy,
                contentColor = CompanionDesign.CardWhite,
                shape = CompanionDesign.ButtonShape,
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
