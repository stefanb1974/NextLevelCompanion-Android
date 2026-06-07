package nl.nextlevelpilots.companion.documents

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign

private val PageShape = RoundedCornerShape(12.dp)

@Composable
fun DocumentPdfViewerScreen(
    documentId: String,
    viewModel: DocumentsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pdfState by viewModel.pdfViewerState.collectAsState()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    LaunchedEffect(documentId, screenWidthPx) {
        viewModel.loadPdfViewer(documentId = documentId, targetWidthPx = screenWidthPx)
    }

    DisposableEffect(documentId) {
        onDispose { viewModel.clearPdfViewer() }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        DocumentPdfTopBar(
            title = pdfState.title ?: "Document",
            onBack = onBack,
        )

        when {
            pdfState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            color = CompanionDesign.Accent,
                            strokeWidth = 2.5.dp,
                        )
                        Text(
                            text = "PDF laden…",
                            color = CompanionDesign.TextSecondary,
                            fontSize = 15.sp,
                        )
                    }
                }
            }

            pdfState.loadFailed -> {
                DocumentPdfErrorState(
                    message = pdfState.errorMessage ?: DocumentsRepository.DOWNLOAD_ERROR_MESSAGE,
                    onRetry = { viewModel.loadPdfViewer(documentId, screenWidthPx) },
                )
            }

            pdfState.pages.isEmpty() -> {
                DocumentPdfErrorState(
                    message = "Geen PDF-pagina's om weer te geven.",
                    onRetry = { viewModel.loadPdfViewer(documentId, screenWidthPx) },
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = CompanionDesign.ScreenPadding,
                        vertical = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    itemsIndexed(
                        items = pdfState.pages,
                        key = { index, _ -> "$documentId-page-$index" },
                    ) { index, pageBitmap ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = PageShape,
                            colors = CardDefaults.cardColors(containerColor = CompanionDesign.CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Image(
                                bitmap = pageBitmap.asImageBitmap(),
                                contentDescription = "Pagina ${index + 1}",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.FillWidth,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentPdfTopBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Terug",
                tint = CompanionDesign.Navy,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Document",
                color = CompanionDesign.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
            )
            Text(
                text = title,
                color = CompanionDesign.Navy,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DocumentPdfErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CompanionDesign.ScreenPadding),
        contentAlignment = Alignment.Center,
    ) {
        CompanionCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = message,
                    color = CompanionDesign.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CompanionDesign.ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CompanionDesign.Accent,
                        contentColor = CompanionDesign.CardWhite,
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
}
