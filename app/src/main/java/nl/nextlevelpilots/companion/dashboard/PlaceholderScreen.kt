package nl.nextlevelpilots.companion.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TabPlaceholderContent(
    title: String,
    modifier: Modifier = Modifier,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
) {
    val lightGrey = Color(0xFFB8BCD4)
    val glassBackground = Color.White.copy(alpha = 0.06f)
    val glassBorder = Color.White.copy(alpha = 0.12f)
    val cardShape = RoundedCornerShape(28.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, glassBorder, cardShape),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = glassBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Binnenkort beschikbaar",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Deze sectie wordt binnenkort toegevoegd aan NextLevel Companion.",
                    color = lightGrey,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (footer != null) {
            Spacer(modifier = Modifier.height(24.dp))
            footer()
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    onBack: () -> Unit,
) {
    val gradientTop = Color(0xFF22287A)
    val gradientBottom = Color(0xFF3439A8)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientTop, gradientBottom),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 12.dp, top = 20.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Terug",
                    tint = Color.White,
                )
            }

            TabPlaceholderContent(title = title)
        }
    }
}
