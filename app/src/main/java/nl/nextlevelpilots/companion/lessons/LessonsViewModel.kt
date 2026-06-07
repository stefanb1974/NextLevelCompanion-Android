package nl.nextlevelpilots.companion.lessons

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nl.nextlevelpilots.companion.auth.SessionStore

data class LessonsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadFailed: Boolean = false,
    val errorMessage: String? = null,
    val allLessons: List<LessonUiModel> = emptyList(),
    val lessons: List<LessonUiModel> = emptyList(),
    val lessonsByDate: List<LessonDateGroup> = emptyList(),
    val nextUpcomingLesson: LessonUiModel? = null,
    val confirmingLessonId: String? = null,
    val snackbarMessage: String? = null,
)

class LessonsViewModel(
    private val repository: LessonsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonsUiState())
    val uiState: StateFlow<LessonsUiState> = _uiState.asStateFlow()

    init {
        loadLessons()
    }

    fun loadLessons() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    errorMessage = null,
                )
            }

            when (val result = repository.loadLessons()) {
                is LessonsRepository.LoadResult.Success -> {
                    applyLessons(
                        lessons = result.lessons,
                        isLoading = false,
                        loadFailed = false,
                        errorMessage = null,
                    )
                }

                is LessonsRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                            errorMessage = result.userMessage,
                            allLessons = emptyList(),
                            lessons = emptyList(),
                            lessonsByDate = emptyList(),
                            nextUpcomingLesson = null,
                        )
                    }
                }
            }
        }
    }

    fun refreshLessons() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            when (val result = repository.loadLessons()) {
                is LessonsRepository.LoadResult.Success -> {
                    applyLessons(
                        lessons = result.lessons,
                        isLoading = false,
                        loadFailed = false,
                        errorMessage = null,
                        isRefreshing = false,
                    )
                }

                is LessonsRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(isRefreshing = false)
                    }
                }
            }
        }
    }

    fun confirmLesson(lessonId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    confirmingLessonId = lessonId,
                    snackbarMessage = null,
                )
            }

            repository.confirmLesson(lessonId)
                .onSuccess {
                    refreshLessonsQuietly()
                    _uiState.update {
                        it.copy(
                            confirmingLessonId = null,
                            snackbarMessage = LessonsRepository.CONFIRM_SUCCESS_MESSAGE,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            confirmingLessonId = null,
                            snackbarMessage = LessonsRepository.CONFIRM_ERROR_MESSAGE,
                        )
                    }
                }
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun lessonById(lessonId: String): LessonUiModel? {
        return findLessonById(_uiState.value.allLessons, lessonId)
    }

    private suspend fun refreshLessonsQuietly() {
        when (val result = repository.loadLessons()) {
            is LessonsRepository.LoadResult.Success -> {
                applyLessons(
                    lessons = result.lessons,
                    isLoading = false,
                    loadFailed = false,
                    errorMessage = null,
                )
            }

            is LessonsRepository.LoadResult.Error -> Unit
        }
    }

    private fun applyLessons(
        lessons: List<LessonUiModel>,
        isLoading: Boolean,
        loadFailed: Boolean,
        errorMessage: String?,
        isRefreshing: Boolean = false,
    ) {
        val upcomingLessons = filterUpcomingLessons(lessons)
        _uiState.update {
            it.copy(
                isLoading = isLoading,
                isRefreshing = isRefreshing,
                loadFailed = loadFailed,
                errorMessage = errorMessage,
                allLessons = lessons,
                lessons = upcomingLessons,
                lessonsByDate = groupLessonsByDate(upcomingLessons),
                nextUpcomingLesson = findNextUpcomingLesson(lessons),
            )
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val sessionStore = SessionStore(context.applicationContext)
            val repository = LessonsRepository(sessionStore)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LessonsViewModel(repository) as T
                }
            }
        }
    }
}
