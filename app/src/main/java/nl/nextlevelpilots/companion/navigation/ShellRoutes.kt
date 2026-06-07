package nl.nextlevelpilots.companion.navigation

object ShellRoutes {
    const val MAIN = "main"
    const val LESSON_DETAIL = "lesson/{lessonId}"
    const val DOCUMENT_PDF_VIEWER = "documents/pdfViewer/{documentId}"

    fun lessonDetail(lessonId: String): String = "lesson/$lessonId"

    fun documentPdfViewer(documentId: String): String = "documents/pdfViewer/$documentId"
}
