package nl.nextlevelpilots.companion.documents

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

data class PdfPageBitmap(
    val pageIndex: Int,
    val bitmap: Bitmap,
)

object PdfPageLoader {

    suspend fun renderPages(
        file: File,
        targetWidthPx: Int,
    ): Result<List<Bitmap>> = withContext(Dispatchers.IO) {
        if (!file.exists() || file.length() == 0L) {
            return@withContext Result.failure(IOException("PDF file is missing or empty"))
        }

        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    val pages = mutableListOf<PdfPageBitmap>()

                    for (pageIndex in 0 until renderer.pageCount) {
                        renderer.openPage(pageIndex).use { page ->
                            val scale = targetWidthPx.toFloat() / page.width.toFloat()
                            val width = targetWidthPx
                            val height = (page.height * scale).toInt().coerceAtLeast(1)
                            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            bitmap.eraseColor(Color.WHITE)
                            val matrix = Matrix().apply { setScale(scale, scale) }
                            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            pages.add(
                                PdfPageBitmap(
                                    pageIndex = pageIndex,
                                    bitmap = bitmap,
                                ),
                            )
                        }
                    }

                    pages.map { it.bitmap }
                }
            }
        }
    }
}
