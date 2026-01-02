package ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

sealed class CalendarUiState {
    data object Loading : CalendarUiState()

    data class Success(
        val year: Int,
        val month: Int,
        val days: List<DayItem>,
    ) : CalendarUiState()

    data class Error(
        val year: Int,
        val month: Int,
        val message: String,
    ) : CalendarUiState()
}

data class DayItem(val date: LocalDate, val exists: Boolean)

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val initialYear: Int,
    private val initialMonth: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            val calendar = calendarRepository.findByMonth(YearMonth(initialYear, initialMonth))

            if (calendar.dates.isEmpty()) {
                _uiState.value = CalendarUiState.Error(
                    year = initialYear,
                    month = initialMonth,
                    message = "Failed to load"
                )
            } else {
                val items = calendar.dates.map { (date, hasContent) ->
                    DayItem(date, hasContent)
                }
                _uiState.value = CalendarUiState.Success(
                    year = initialYear,
                    month = initialMonth,
                    days = items
                )
            }
        }
    }
}
