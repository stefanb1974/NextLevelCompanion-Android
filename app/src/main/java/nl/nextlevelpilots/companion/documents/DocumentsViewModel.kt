package nl.nextlevelpilots.companion.documents

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.nextlevelpilots.companion.auth.SessionStore

data class DocumentsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadFailed: Boolean = false,
    val errorMessage: String? = null,
    val documents: List<DocumentUiModel> = emptyList(),
    val snackbarMessage: String? = null,
)

data class DocumentPdfViewerUiState(
    val documentId: String? = null,
    val title: String? = null,
    val isLoading: Boolean = false,
    val loadFailed: Boolean = false,
    val errorMessage: String? = null,
    val pages: List<Bitmap> = emptyList(),
)

class DocumentsViewModel(
    private val repository: DocumentsRepository,
    private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    private val _pdfViewerState = MutableStateFlow(DocumentPdfViewerUiState())
    val pdfViewerState: StateFlow<DocumentPdfViewerUiState> = _pdfViewerState.asStateFlow()

    init {
        loadDocuments()
    }

    fun documentById(documentId: String): DocumentUiModel? {
        return _uiState.value.documents.find { it.id == documentId }
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    errorMessage = null,
                )
            }

            when (val result = repository.loadDocuments()) {
                is DocumentsRepository.LoadResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = false,
                            errorMessage = null,
                            documents = result.documents,
                        )
                    }
                }

                is DocumentsRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                            errorMessage = result.userMessage,
                            documents = emptyList(),
                        )
                    }
                }
            }
        }
    }

    fun refreshDocuments() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            when (val result = repository.loadDocuments()) {
                is DocumentsRepository.LoadResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            loadFailed = false,
                            errorMessage = null,
                            documents = result.documents,
                        )
                    }
                }

                is DocumentsRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(isRefreshing = false)
                    }
                }
            }
        }
    }

    fun loadPdfViewer(documentId: String, targetWidthPx: Int) {
        if (_pdfViewerState.value.isLoading && _pdfViewerState.value.documentId == documentId) return

        viewModelScope.launch {
            clearPdfPageBitmaps()
            val title = documentById(documentId)?.title ?: "Document"

            _pdfViewerState.value = DocumentPdfViewerUiState(
                documentId = documentId,
                title = title,
                isLoading = true,
            )

            when (
                val downloadResult = repository.downloadDocument(
                    documentId = documentId,
                    cacheDir = DocumentsRepository.documentsCacheDir(appContext),
                )
            ) {
                is DocumentsRepository.DownloadResult.Success -> {
                    if (!downloadResult.mimeType.contains("pdf", ignoreCase = true)) {
                        _pdfViewerState.value = DocumentPdfViewerUiState(
                            documentId = documentId,
                            title = title,
                            loadFailed = true,
                            errorMessage = "Dit bestand is geen PDF.",
                        )
                        return@launch
                    }

                    PdfPageLoader.renderPages(downloadResult.file, targetWidthPx)
                        .onSuccess { pages ->
                            _pdfViewerState.value = DocumentPdfViewerUiState(
                                documentId = documentId,
                                title = title,
                                pages = pages,
                            )
                        }
                        .onFailure {
                            _pdfViewerState.value = DocumentPdfViewerUiState(
                                documentId = documentId,
                                title = title,
                                loadFailed = true,
                                errorMessage = DocumentsRepository.DOWNLOAD_ERROR_MESSAGE,
                            )
                        }
                }

                is DocumentsRepository.DownloadResult.Error -> {
                    _pdfViewerState.value = DocumentPdfViewerUiState(
                        documentId = documentId,
                        title = title,
                        loadFailed = true,
                        errorMessage = downloadResult.userMessage,
                    )
                }
            }
        }
    }

    fun clearPdfViewer() {
        clearPdfPageBitmaps()
        _pdfViewerState.value = DocumentPdfViewerUiState()
    }

    fun downloadDocumentOffline(documentId: String) {
        viewModelScope.launch {
            when (
                val result = repository.downloadDocument(
                    documentId = documentId,
                    cacheDir = DocumentsRepository.documentsCacheDir(appContext),
                )
            ) {
                is DocumentsRepository.DownloadResult.Success -> {
                    _uiState.update {
                        it.copy(snackbarMessage = "Document opgeslagen voor offline gebruik")
                    }
                }

                is DocumentsRepository.DownloadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = result.userMessage
                                ?: DocumentsRepository.DOWNLOAD_ERROR_MESSAGE,
                        )
                    }
                }
            }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    private fun clearPdfPageBitmaps() {
        _pdfViewerState.value.pages.forEach { bitmap ->
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
    }

    override fun onCleared() {
        clearPdfViewer()
        super.onCleared()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            val sessionStore = SessionStore(appContext)
            val repository = DocumentsRepository(sessionStore)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DocumentsViewModel(repository, appContext) as T
                }
            }
        }
    }
}
