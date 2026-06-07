package nl.nextlevelpilots.companion.lessons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionDesign
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LessonDateBadge(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.forLanguageTag("nl-NL")
    val day = date.dayOfMonth.toString()
    val month = date.format(DateTimeFormatter.ofPattern("MMM", locale))
        .replace(".", "")
        .uppercase(locale)

    Column(
        modifier = modifier
            .width(56.dp)
            .background(CompanionDesign.Background, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = day,
            color = CompanionDesign.Navy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = month,
            color = CompanionDesign.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
