package ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.model.Result
import domain.usecase.FetchMonthDiariesUseCase
import java.time.LocalDate

data class CalendarState(
    val year: Int,
    val month: Int,
    val days: List<DayItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class DayItem(val date: LocalDate, val exists: Boolean)

class CalendarViewModel(
    private val fetchMonthDiariesUseCase: FetchMonthDiariesUseCase,
    initialYear: Int,
    initialMonth: Int
) {
    var state by mutableStateOf(CalendarState(initialYear, initialMonth))
        private set

    suspend fun load(owner: String, repo: String) {
        state = state.copy(isLoading = true, error = null)
        when (val result = fetchMonthDiariesUseCase(owner, repo, state.year, state.month)) {
            is Result.Success -> {
                val items = result.value.map { DayItem(it.date, it.exists) }
                state = state.copy(days = items, isLoading = false)
            }

            is Result.Failure -> {
                state = state.copy(isLoading = false, error = result.message ?: "Failed to load")
            }
        }
    }

    fun nextMonth() {
        val next = LocalDate.of(state.year, state.month, 1).plusMonths(1)
        state = state.copy(year = next.year, month = next.monthValue)
    }

    fun prevMonth() {
        val prev = LocalDate.of(state.year, state.month, 1).minusMonths(1)
        state = state.copy(year = prev.year, month = prev.monthValue)
    }
}
