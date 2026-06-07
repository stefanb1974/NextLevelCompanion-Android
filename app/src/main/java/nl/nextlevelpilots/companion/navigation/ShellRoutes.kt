package nl.nextlevelpilots.companion.navigation

object ShellRoutes {
    const val MAIN = "main"
    const val LESSON_DETAIL = "lesson/{lessonId}"

    fun lessonDetail(lessonId: String): String = "lesson/$lessonId"
}
