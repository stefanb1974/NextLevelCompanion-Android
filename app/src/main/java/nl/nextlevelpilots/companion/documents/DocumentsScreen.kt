package nl.nextlevelpilots.companion.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.PremiumPullRefresh

private val LightGrey = Color(0xFFB8BCD4)
private val MutedGrey = Color(0xFF9AA3BC)
private val GlassBackground = Color.White.copy(alpha = 0.06f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val AccentOrange = Color(0xFFFF8B56)
private val CardShape = RoundedCornerShape(28.dp)
private val DocumentCardShape = RoundedCornerShape(22.dp)
private val EasaBlueBackground = Color(0xFFB8D4F8)
private val EasaBlueText = Color(0xFF1A4F8C)
private val ReadGreenBackground = Color(0xFFB8F0CE)
private val ReadGreenText = Color(0xFF137A3A)

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    onDocumentClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    PremiumPullRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refreshDocuments,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Documenten",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Je trainingdocumenten en syllabusmaterialen",
                color = LightGrey,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )

            when {
                uiState.isLoading && uiState.documents.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = AccentOrange,
                            strokeWidth = 2.5.dp,
                        )
                    }
                }

                uiState.loadFailed && uiState.documents.isEmpty() -> {
                    DocumentsErrorCard(
                        message = uiState.errorMessage ?: DocumentsRepository.LOAD_ERROR_MESSAGE,
                        onRetry = viewModel::loadDocuments,
                    )
                }

                uiState.documents.isEmpty() -> {
                    DocumentsEmptyCard()
                }

                else -> {
                    uiState.documents.forEach { document ->
                        DocumentCard(
                            document = document,
                            onOpen = { onDocumentClick(document.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(
    document: DocumentUiModel,
    onOpen: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = DocumentCardShape,
                ambientColor = Color.Black.copy(alpha = 0.2f),
                spotColor = Color.Black.copy(alpha = 0.15f),
            )
            .border(1.dp, GlassBorder, DocumentCardShape),
        shape = DocumentCardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = document.title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                document.typeLabel?.let { typeLabel ->
                    DocumentMetaBadge(
                        text = typeLabel,
                        background = Color.White.copy(alpha = 0.12f),
                        textColor = Color.White,
                    )
                }
                document.statusLabel?.let { statusLabel ->
                    DocumentMetaBadge(
                        text = statusLabel,
                        background = Color.White.copy(alpha = 0.10f),
                        textColor = LightGrey,
                    )
                }
                if (document.isEasa) {
                    DocumentMetaBadge(
                        text = "EASA",
                        background = EasaBlueBackground,
                        textColor = EasaBlueText,
                    )
                }
                if (document.isRead) {
                    DocumentMetaBadge(
                        text = "Gelezen",
                        background = ReadGreenBackground,
                        textColor = ReadGreenText,
                    )
                }
            }

            document.updatedAtLabel?.let { updatedAt ->
                Text(
                    text = "Bijgewerkt $updatedAt",
                    color = MutedGrey,
                    fontSize = 13.sp,
                )
            }

            Button(
                onClick = onOpen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Openen",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DocumentMetaBadge(
    text: String,
    background: Color,
    textColor: Color,
) {
    Text(
        text = text,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .background(background, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
private fun DocumentsEmptyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Geen documenten",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Er zijn nog geen documenten beschikbaar voor jouw account.",
                color = LightGrey,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DocumentsErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder, CardShape),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = GlassBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentOrange,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Opnieuw proberen",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
