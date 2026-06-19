package nl.nextlevelpilots.companion.trainingprogress

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

data class TrainingProgressUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val loadFailed: Boolean = false,
    val errorMessage: String? = null,
    val courses: List<TrainingProgressCourseUiModel> = emptyList(),
    val role: String? = null,
    val instructorSummary: String? = null,
)

class TrainingProgressViewModel(
    private val repository: TrainingProgressRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingProgressUiState())
    val uiState: StateFlow<TrainingProgressUiState> = _uiState.asStateFlow()

    init {
        loadTrainingProgress()
    }

    fun loadTrainingProgress() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    errorMessage = null,
                )
            }

            when (val result = repository.loadTrainingProgress()) {
                is TrainingProgressRepository.LoadResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = false,
                            errorMessage = null,
                            courses = result.courses,
                            role = result.role,
                            instructorSummary = buildInstructorSummary(result),
                        )
                    }
                }

                is TrainingProgressRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                            errorMessage = result.userMessage,
                            courses = emptyList(),
                        )
                    }
                }
            }
        }
    }

    fun refreshTrainingProgress() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            when (val result = repository.loadTrainingProgress()) {
                is TrainingProgressRepository.LoadResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            loadFailed = false,
                            errorMessage = null,
                            courses = result.courses,
                            role = result.role,
                            instructorSummary = buildInstructorSummary(result),
                        )
                    }
                }

                is TrainingProgressRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            loadFailed = it.courses.isEmpty(),
                            errorMessage = if (it.courses.isEmpty()) result.userMessage else it.errorMessage,
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            val sessionStore = SessionStore(appContext)
            val repository = TrainingProgressRepository(sessionStore)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TrainingProgressViewModel(repository) as T
                }
            }
        }

        private fun buildInstructorSummary(
            result: TrainingProgressRepository.LoadResult.Success,
        ): String? {
            if (result.role?.trim()?.lowercase() != "instructor") return null
            val count = result.activeStudentCount ?: return null
            return if (count == 1) {
                "1 student actief"
            } else {
                "$count studenten actief"
            }
        }
    }
}
