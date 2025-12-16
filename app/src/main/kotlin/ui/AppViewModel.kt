package ui

import core.time.DateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class AppState(
    val currentRoute: String = "Calendar",
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AppViewModel(
    private val dateProvider: DateProvider
) {
    private val _state = MutableStateFlow(AppState(selectedDate = dateProvider.today()))
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun navigate(route: String, date: LocalDate? = _state.value.selectedDate) {
        _state.value = _state.value.copy(currentRoute = route, selectedDate = date)
    }

    fun goToday() {
        _state.value = _state.value.copy(selectedDate = dateProvider.today())
    }

    fun setLoading(loading: Boolean) {
        _state.value = _state.value.copy(isLoading = loading)
    }

    fun setError(message: String?) {
        _state.value = _state.value.copy(errorMessage = message)
    }
}