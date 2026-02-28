package io.github.t45k.githubDiary.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth

sealed interface CalendarUiState {
    val yearMonth: YearMonth

    data class Loading(override val yearMonth: YearMonth) : CalendarUiState

    data class Success(
        override val yearMonth: YearMonth,
        val calendar: Calendar,
    ) : CalendarUiState

    data class Error(
        override val yearMonth: YearMonth,
        val message: String,
    ) : CalendarUiState
}

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    calendarRefreshEvent: CalendarRefreshEvent,
    yearMonth: YearMonth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading(yearMonth))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        load(yearMonth)
        calendarRefreshEvent.refreshRequest
            .filter { it == yearMonth }
            .onEach { reload() }
            .launchIn(viewModelScope)
    }

    fun reload() {
        load(_uiState.value.yearMonth)
    }

    private fun load(yearMonth: YearMonth) {
        _uiState.value = CalendarUiState.Loading(yearMonth)

        viewModelScope.launch {
            val calendar = calendarRepository.findByMonth(yearMonth)

            if (calendar.days.isEmpty()) {
                _uiState.value = CalendarUiState.Error(
                    yearMonth,
                    message = "Failed to load",
                )
            } else {
                _uiState.value = CalendarUiState.Success(
                    yearMonth = yearMonth,
                    calendar = calendar,
                )
            }
        }
    }
}
