package nl.nextlevelpilots.companion.dashboard

import nl.nextlevelpilots.companion.ui.CompanionDesign

typealias DashboardTheme = CompanionDesign

fun firstNameFrom(fullName: String?): String? {
    return fullName
        ?.trim()
        ?.split(" ")
        ?.firstOrNull()
        ?.takeIf { it.isNotBlank() }
}
