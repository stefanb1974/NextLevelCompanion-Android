package nl.nextlevelpilots.companion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import nl.nextlevelpilots.companion.LoginScreen
import nl.nextlevelpilots.companion.auth.SessionStore

object AppRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNav(
    startDestination: String,
    sessionStore: SessionStore,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { token, name, email, role, linkedPersonId ->
                    scope.launch {
                        sessionStore.saveSession(
                            token = token,
                            userName = name,
                            userEmail = email,
                            userRole = role,
                            linkedPersonId = linkedPersonId,
                        )
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(AppRoutes.DASHBOARD) {
            val userName by sessionStore.userNameFlow.collectAsState(initial = null)
            val userEmail by sessionStore.userEmailFlow.collectAsState(initial = null)
            val userRole by sessionStore.userRoleFlow.collectAsState(initial = null)
            val linkedPersonId by sessionStore.linkedPersonIdFlow.collectAsState(initial = null)

            MainShellScreen(
                userName = userName,
                userEmail = userEmail,
                userRole = userRole,
                linkedPersonId = linkedPersonId,
                onLogout = {
                    scope.launch {
                        sessionStore.clearSession()
                        navController.navigate(AppRoutes.LOGIN) {
                            popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                        }
                    }
                },
            )
        }
    }
}
