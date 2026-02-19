package io.github.t45k.githubDiary.ui

import androidx.lifecycle.ViewModel
import io.github.t45k.githubDiary.util.DateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.YearMonth

class AppViewModel(private val dateProvider: DateProvider) : ViewModel() {
    private val _backStack = MutableStateFlow<List<NavRoute>>(
        listOf(NavRoute.Calendar(dateProvider.currentYearMonth()))
    )
    val backStack: StateFlow<List<NavRoute>> = _backStack.asStateFlow()

    fun navigateToCalendar(yearMonth: YearMonth) {
        _backStack.value = listOf(NavRoute.Calendar(yearMonth))
    }

    fun navigateToToday() {
        navigateToCalendar(dateProvider.currentYearMonth())
    }

    fun push(route: NavRoute) {
        _backStack.value = _backStack.value + route
    }

    fun pop() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
        }
    }
}
