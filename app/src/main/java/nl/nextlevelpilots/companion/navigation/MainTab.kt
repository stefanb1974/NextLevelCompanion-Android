package nl.nextlevelpilots.companion.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    TRAINING("Trainingen", Icons.Default.School),
    AVAILABILITY("Beschikbaarheid", Icons.Default.CalendarMonth),
    PROFILE("Profiel", Icons.Default.Person),
}
