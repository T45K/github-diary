package io.github.t45k.githubDiary.ui.goal.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.t45k.githubDiary.core.entity.GoalContent
import io.github.t45k.githubDiary.core.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth

sealed interface GoalPreviewUiState {
    val yearMonth: YearMonth

    data class Loading(override val yearMonth: YearMonth) : GoalPreviewUiState

    data class Success(
        override val yearMonth: YearMonth,
        val content: GoalContent,
    ) : GoalPreviewUiState

    data class Error(
        override val yearMonth: YearMonth,
        val message: String,
    ) : GoalPreviewUiState
}

class GoalPreviewViewModel(
    private val goalRepository: GoalRepository,
    yearMonth: YearMonth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<GoalPreviewUiState>(GoalPreviewUiState.Loading(yearMonth))
    val uiState: StateFlow<GoalPreviewUiState> = _uiState.asStateFlow()

    init {
        load(yearMonth)
    }

    private fun load(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = GoalPreviewUiState.Loading(yearMonth)

            try {
                _uiState.value = GoalPreviewUiState.Success(
                    yearMonth,
                    goalRepository.findByYearMonth(yearMonth),
                )
            } catch (e: Exception) {
                _uiState.value = GoalPreviewUiState.Error(
                    yearMonth,
                    message = e.message ?: "Unknown error",
                )
            }
        }
    }
}
