package nl.nextlevelpilots.companion.availability

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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields

data class AvailabilityUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val loadFailed: Boolean = false,
    val loadErrorMessage: String? = null,
    val saveErrorMessage: String? = null,
    val currentMonth: YearMonth = YearMonth.now(),
    val days: Map<LocalDate, DayAvailability> = emptyMap(),
    val savedDays: Map<LocalDate, DayAvailability> = emptyMap(),
    val selectedDay: LocalDate? = null,
    val hasPendingChanges: Boolean = false,
)

data class CalendarCell(
    val date: LocalDate?,
    val dayNumber: Int?,
)

class AvailabilityViewModel(
    private val repository: AvailabilityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailabilityUiState())
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    private var availabilityByMonth: Map<String, Map<String, AvailabilityDayEntry>> = emptyMap()
    private val pendingChanges = mutableMapOf<LocalDate, DayAvailability>()

    init {
        loadAvailability()
    }

    fun loadAvailability() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    loadErrorMessage = null,
                    saveErrorMessage = null,
                )
            }

            val month = _uiState.value.currentMonth
            when (val result = repository.loadAvailability(month)) {
                is AvailabilityRepository.LoadResult.Success -> {
                    availabilityByMonth = availabilityByMonth + result.availabilityByMonth
                    applyMonth(month)
                }

                is AvailabilityRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                            loadErrorMessage = result.userMessage,
                        )
                    }
                }
            }
        }
    }

    fun refreshCurrentMonth() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            val month = _uiState.value.currentMonth
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    loadFailed = false,
                    loadErrorMessage = null,
                )
            }

            when (val result = repository.loadAvailability(month)) {
                is AvailabilityRepository.LoadResult.Success -> {
                    availabilityByMonth = repository.replaceMonthsInCache(
                        existing = availabilityByMonth,
                        incoming = result.availabilityByMonth,
                    )
                    reconcilePendingChangesWithServer(month)
                    applyMonth(month, isRefreshing = false)
                }

                is AvailabilityRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(isRefreshing = false)
                    }
                }
            }
        }
    }

    fun previousMonth() {
        changeMonth(_uiState.value.currentMonth.minusMonths(1))
    }

    fun nextMonth() {
        changeMonth(_uiState.value.currentMonth.plusMonths(1))
    }

    fun onDayTapped(date: LocalDate) {
        val current = pendingChanges[date] ?: serverAvailabilityFor(date)
        applyPendingChange(date, current.onSingleTap())
        refreshCurrentMonthState(selectedDay = date)
    }

    fun setDayAvailability(date: LocalDate, availability: DayAvailability) {
        applyPendingChange(date, availability)
        refreshCurrentMonthState(selectedDay = date)
    }

    fun applyAvailabilityToDates(dates: Set<LocalDate>, availability: DayAvailability) {
        if (dates.isEmpty()) return
        dates.forEach { date ->
            applyPendingChange(date, availability)
        }
        refreshCurrentMonthState(selectedDay = null)
    }

    fun applyStatusToDates(dates: Set<LocalDate>, status: DayAvailabilityStatus) {
        applyAvailabilityToDates(dates, status.toFullDayAvailability())
    }

    fun availabilityFor(date: LocalDate): DayAvailability {
        return pendingChanges[date] ?: serverAvailabilityFor(date)
    }

    fun saveChanges() {
        val state = _uiState.value
        if (!state.hasPendingChanges || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveErrorMessage = null) }

            val pendingSnapshot = pendingChanges.toMap()
            when (
                val result = repository.savePendingChanges(
                    pendingChanges = pendingSnapshot,
                    availabilityByMonth = availabilityByMonth,
                )
            ) {
                is AvailabilityRepository.SaveResult.Success -> {
                    availabilityByMonth = repository.mergeAvailabilityByMonth(
                        existing = availabilityByMonth,
                        incoming = result.availabilityByMonth,
                    )

                    val monthsToReload = result.savedDates
                        .map { YearMonth.from(it) }
                        .toSet()
                        .sorted()

                    val reloadedMonths = mutableSetOf<YearMonth>()
                    var reloadFailed = false

                    for (month in monthsToReload) {
                        when (val reload = repository.loadAvailability(month)) {
                            is AvailabilityRepository.LoadResult.Success -> {
                                availabilityByMonth = repository.replaceMonthsInCache(
                                    existing = availabilityByMonth,
                                    incoming = reload.availabilityByMonth,
                                )
                                repository.logMappedDayStates(month, availabilityByMonth)
                                reloadedMonths += month
                            }

                            is AvailabilityRepository.LoadResult.Error -> {
                                reloadFailed = true
                            }
                        }
                    }

                    result.savedDates
                        .filter { YearMonth.from(it) in reloadedMonths }
                        .forEach { pendingChanges.remove(it) }

                    val saveMessage = if (reloadFailed) {
                        AvailabilityRepository.RELOAD_AFTER_SAVE_MESSAGE
                    } else {
                        null
                    }

                    applyMonth(
                        month = state.currentMonth,
                        isSaving = false,
                        saveErrorMessage = saveMessage,
                    )
                }

                is AvailabilityRepository.SaveResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveErrorMessage = result.userMessage,
                        )
                    }
                }
            }
        }
    }

    fun calendarCells(month: YearMonth): List<CalendarCell> {
        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
        val leadingEmptyDays = firstDay.get(weekFields.dayOfWeek()) - 1

        val cells = mutableListOf<CalendarCell>()
        repeat(leadingEmptyDays) {
            cells += CalendarCell(date = null, dayNumber = null)
        }
        for (day in 1..lastDay.dayOfMonth) {
            cells += CalendarCell(
                date = month.atDay(day),
                dayNumber = day,
            )
        }
        while (cells.size % 7 != 0) {
            cells += CalendarCell(date = null, dayNumber = null)
        }
        return cells
    }

    private fun changeMonth(month: YearMonth) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentMonth = month,
                    isLoading = true,
                    loadFailed = false,
                    loadErrorMessage = null,
                    saveErrorMessage = null,
                    selectedDay = null,
                )
            }

            when (val result = repository.loadAvailability(month)) {
                is AvailabilityRepository.LoadResult.Success -> {
                    availabilityByMonth = availabilityByMonth + result.availabilityByMonth
                    applyMonth(month)
                }

                is AvailabilityRepository.LoadResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadFailed = true,
                            loadErrorMessage = result.userMessage,
                        )
                    }
                }
            }
        }
    }

    private fun applyMonth(
        month: YearMonth,
        isSaving: Boolean = false,
        saveErrorMessage: String? = null,
        isRefreshing: Boolean = false,
    ) {
        val serverDays = repository.monthAvailabilityFrom(availabilityByMonth, month)
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = isRefreshing,
                isSaving = isSaving,
                currentMonth = month,
                days = displayDaysForMonth(month),
                savedDays = serverDays,
                selectedDay = null,
                loadFailed = false,
                loadErrorMessage = null,
                saveErrorMessage = saveErrorMessage,
                hasPendingChanges = pendingChanges.isNotEmpty(),
            )
        }
    }

    private fun refreshCurrentMonthState(selectedDay: LocalDate? = null) {
        val month = _uiState.value.currentMonth
        _uiState.update {
            it.copy(
                selectedDay = selectedDay,
                days = displayDaysForMonth(month),
                hasPendingChanges = pendingChanges.isNotEmpty(),
            )
        }
    }

    private fun serverAvailabilityFor(date: LocalDate): DayAvailability {
        return repository.monthAvailabilityFrom(availabilityByMonth, YearMonth.from(date))[date]
            ?: DayAvailability.notSet()
    }

    private fun displayDaysForMonth(month: YearMonth): Map<LocalDate, DayAvailability> {
        val displayDays = repository.monthAvailabilityFrom(availabilityByMonth, month).toMutableMap()
        pendingChanges.forEach { (date, availability) ->
            if (YearMonth.from(date) == month) {
                displayDays[date] = availability
            }
        }
        return displayDays
    }

    private fun applyPendingChange(date: LocalDate, newAvailability: DayAvailability) {
        val serverAvailability = serverAvailabilityFor(date)
        if (newAvailability == serverAvailability) {
            pendingChanges.remove(date)
        } else {
            pendingChanges[date] = newAvailability
        }
    }

    private fun reconcilePendingChangesWithServer(month: YearMonth) {
        pendingChanges.keys
            .filter { YearMonth.from(it) == month }
            .toList()
            .forEach { date ->
                val serverAvailability = serverAvailabilityFor(date)
                val pendingAvailability = pendingChanges[date] ?: return@forEach
                if (pendingAvailability == serverAvailability) {
                    pendingChanges.remove(date)
                }
            }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val sessionStore = SessionStore(context.applicationContext)
            val repository = AvailabilityRepository(sessionStore)
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AvailabilityViewModel(repository) as T
                }
            }
        }
    }
}
