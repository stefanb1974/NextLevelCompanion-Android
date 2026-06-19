package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LessonStatusBadge(
    label: String,
    status: LessonStatus,
    modifier: Modifier = Modifier,
) {
    val (background, textColor) = when (status) {
        LessonStatus.SUGGESTED -> Color(0xFFFFE4CC) to Color(0xFF8A4D00)
        LessonStatus.PLANNED -> Color(0xFFFFD9A8) to Color(0xFF8A4D00)
        LessonStatus.CONFIRMED -> Color(0xFFB8F0CE) to Color(0xFF137A3A)
        LessonStatus.COMPLETED -> Color(0xFFD4D8E8) to Color(0xFF4A4F63)
        LessonStatus.CANCELLED -> Color(0xFFF8B4B4) to Color(0xFFB3261E)
        LessonStatus.UNKNOWN -> Color(0xFFE2E5F0) to Color(0xFF4A4F63)
    }

    Text(
        text = label,
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
