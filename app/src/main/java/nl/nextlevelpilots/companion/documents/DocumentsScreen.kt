package nl.nextlevelpilots.companion.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.nextlevelpilots.companion.ui.CompanionCard
import nl.nextlevelpilots.companion.ui.CompanionDesign
import nl.nextlevelpilots.companion.ui.PremiumPullRefresh
import java.io.File

private enum class DocumentFilter(val label: String) {
    ALL("Alles"),
    SYLLABUS("Syllabus"),
    EASA("EASA"),
    UNREAD("Ongelezen"),
}

private val EasaBlueBackground = Color(0xFFB8D4F8)
private val EasaBlueText = Color(0xFF1A4F8C)
private val ReadGreenBackground = Color(0xFFB8F0CE)
private val ReadGreenText = Color(0xFF137A3A)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    onDocumentClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(DocumentFilter.ALL) }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    val filteredDocuments = remember(uiState.documents, searchQuery, selectedFilter) {
        uiState.documents.filter { document ->
            val matchesSearch = searchQuery.isBlank() ||
                document.title.contains(searchQuery, ignoreCase = true) ||
                document.typeLabel?.contains(searchQuery, ignoreCase = true) == true

            val matchesFilter = when (selectedFilter) {
                DocumentFilter.ALL -> true
                DocumentFilter.SYLLABUS -> document.typeLabel?.equals("Syllabus", ignoreCase = true) == true
                DocumentFilter.EASA -> document.isEasa
                DocumentFilter.UNREAD -> !document.isRead
            }

            matchesSearch && matchesFilter
        }
    }

    PremiumPullRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refreshDocuments,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = CompanionDesign.ScreenPadding,
                vertical = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(CompanionDesign.ItemSpacing),
        ) {
            item {
                Text(
                    text = "Documenten",
                    color = CompanionDesign.Navy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Zoeken", color = CompanionDesign.TextTertiary)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CompanionDesign.TextSecondary,
                        )
                    },
                    singleLine = true,
                    shape = CompanionDesign.ChipShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CompanionDesign.CardWhite,
                        unfocusedContainerColor = CompanionDesign.CardWhite,
                        focusedBorderColor = CompanionDesign.Border,
                        unfocusedBorderColor = CompanionDesign.Border,
                        focusedTextColor = CompanionDesign.TextPrimary,
                        unfocusedTextColor = CompanionDesign.TextPrimary,
                    ),
                )
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DocumentFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontSize = 13.sp,
                                )
                            },
                            shape = CompanionDesign.ChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CompanionDesign.Navy,
                                selectedLabelColor = CompanionDesign.CardWhite,
                                containerColor = CompanionDesign.CardWhite,
                                labelColor = CompanionDesign.TextSecondary,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedFilter == filter,
                                borderColor = CompanionDesign.Border,
                                selectedBorderColor = CompanionDesign.Navy,
                            ),
                        )
                    }
                }
            }

            when {
                uiState.isLoading && uiState.documents.isEmpty() && !uiState.isRefreshing -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                color = CompanionDesign.Accent,
                                strokeWidth = 2.5.dp,
                            )
                        }
                    }
                }

                uiState.loadFailed && uiState.documents.isEmpty() -> {
                    item {
                        DocumentsErrorCard(
                            message = uiState.errorMessage ?: DocumentsRepository.LOAD_ERROR_MESSAGE,
                            onRetry = viewModel::loadDocuments,
                        )
                    }
                }

                uiState.documents.isEmpty() -> {
                    item { DocumentsEmptyCard() }
                }

                filteredDocuments.isEmpty() -> {
                    item { DocumentsNoResultsCard() }
                }

                else -> {
                    items(
                        items = filteredDocuments,
                        key = { document -> document.id },
                    ) { document ->
                        val isExpanded = expandedStates[document.id] == true
                        DocumentCard(
                            document = document,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedStates[document.id] = !isExpanded
                            },
                            onOpen = { onDocumentClick(document.id) },
                            onDownloadOffline = { viewModel.downloadDocumentOffline(document.id) },
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
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpen: () -> Unit,
    onDownloadOffline: () -> Unit,
) {
    val context = LocalContext.current
    val isCached = remember(document.id) {
        isDocumentCached(context.cacheDir, document.id)
    }

    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CompanionDesign.CardPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleExpand,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = document.title,
                    color = CompanionDesign.Navy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Inklappen" else "Uitklappen",
                    tint = CompanionDesign.TextSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }

            DocumentBadgesRow(document = document)

            if (isExpanded) {
                document.updatedAtLabel?.let { updatedAt ->
                    Text(
                        text = "Bijgewerkt $updatedAt",
                        color = CompanionDesign.TextSecondary,
                        fontSize = 13.sp,
                    )
                }

                document.statusLabel?.let { status ->
                    Text(
                        text = status,
                        color = CompanionDesign.TextTertiary,
                        fontSize = 13.sp,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onOpen,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = CompanionDesign.ButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CompanionDesign.Accent,
                            contentColor = CompanionDesign.CardWhite,
                        ),
                    ) {
                        Text(
                            text = "Openen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    TextButton(
                        onClick = onDownloadOffline,
                        modifier = Modifier.height(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = CompanionDesign.Navy,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = if (isCached) "Opgeslagen" else "Offline",
                            color = CompanionDesign.Navy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DocumentBadgesRow(document: DocumentUiModel) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        document.typeLabel?.let { typeLabel ->
            DocumentMetaBadge(
                text = typeLabel,
                background = CompanionDesign.Background,
                textColor = CompanionDesign.Navy,
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
        } else {
            DocumentMetaBadge(
                text = "Ongelezen",
                background = CompanionDesign.Border,
                textColor = CompanionDesign.TextSecondary,
            )
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
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Geen documenten",
                color = CompanionDesign.Navy,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Er zijn nog geen documenten beschikbaar voor jouw account.",
                color = CompanionDesign.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DocumentsNoResultsCard() {
    CompanionCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Geen documenten gevonden",
            color = CompanionDesign.TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
    }
}

@Composable
private fun DocumentsErrorCard(
    message: String,
    onRetry: () -> Unit,
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
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
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
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun isDocumentCached(cacheRoot: File, documentId: String): Boolean {
    val documentsDir = File(cacheRoot, "documents")
    return documentsDir.listFiles()?.any { file ->
        file.name.startsWith("document-$documentId.")
    } == true
}
