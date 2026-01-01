package ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.repository.CalendarRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plusMonth

data class CalendarState(
    val year: Int,
    val month: Int,
    val days: List<DayItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class DayItem(val date: LocalDate, val exists: Boolean)

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    initialYear: Int,
    initialMonth: Int,
) : ViewModel() {
    var state by mutableStateOf(CalendarState(initialYear, initialMonth))
        private set

    fun load() = viewModelScope.launch {
        state = state.copy(isLoading = true, error = null)
        val calendar = calendarRepository.findByMonth(currentDisplayedYearMonth())
        if (calendar.dates.isEmpty()) {
            state = state.copy(isLoading = false, error = "Failed to load")
        } else {
            val items = calendar.dates.map { (date, hasContent) -> DayItem(date, hasContent) }
            state = state.copy(days = items, isLoading = false)
        }
    }

    fun nextMonth() {
        val nextMonth = currentDisplayedYearMonth().plusMonth()
        state = state.copy(year = nextMonth.year, month = nextMonth.month.number)
        load()
    }

    fun prevMonth() {
        val prevMonth = currentDisplayedYearMonth().minusMonth()
        state = state.copy(year = prevMonth.year, month = prevMonth.month.number)
        load()
    }

    private fun currentDisplayedYearMonth(): YearMonth = YearMonth(state.year, state.month)
}
