package io.github.t45k.githubDiary.diary.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.t45k.githubDiary.diary.DiaryContent
import io.github.t45k.githubDiary.diary.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class ReadMoreUiState(
    val initialDate: LocalDate,
    val entries: List<ReadMoreDiaryEntryUiState> = emptyList(),
    val isLoadingPrevious: Boolean = false,
    val isLoadingNext: Boolean = false,
)

sealed interface ReadMoreDiaryEntryUiState {
    val date: LocalDate

    data class Success(
        override val date: LocalDate,
        val content: String,
    ) : ReadMoreDiaryEntryUiState

    data class NotFound(
        override val date: LocalDate,
    ) : ReadMoreDiaryEntryUiState

    data class Error(
        override val date: LocalDate,
        val message: String,
    ) : ReadMoreDiaryEntryUiState
}

class ReadMoreViewModel(
    private val diaryRepository: DiaryRepository,
    private val initialDate: LocalDate,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReadMoreUiState(initialDate = initialDate))
    val uiState: StateFlow<ReadMoreUiState> = _uiState.asStateFlow()

    private var firstLoadedDate: LocalDate = initialDate
    private var lastLoadedDate: LocalDate = initialDate

    init {
        loadInitialEntries()
    }

    fun loadPrevious() {
        if (_uiState.value.isLoadingPrevious) {
            return
        }

        val startDate = firstLoadedDate.minus(DatePeriod(days = LOAD_BATCH_SIZE))
        val endDate = firstLoadedDate.minus(DatePeriod(days = 1))
        loadRange(startDate, endDate, Direction.Previous)
    }

    fun loadNext() {
        if (_uiState.value.isLoadingNext) {
            return
        }

        val startDate = lastLoadedDate.plus(DatePeriod(days = 1))
        val endDate = lastLoadedDate.plus(DatePeriod(days = LOAD_BATCH_SIZE))
        loadRange(startDate, endDate, Direction.Next)
    }

    private fun loadInitialEntries() {
        val startDate = initialDate.minus(DatePeriod(days = INITIAL_SIDE_DAYS))
        val endDate = initialDate.plus(DatePeriod(days = INITIAL_SIDE_DAYS))
        loadRange(startDate, endDate, direction = null)
    }

    private fun loadRange(
        startDate: LocalDate,
        endDate: LocalDate,
        direction: Direction?,
    ) {
        viewModelScope.launch {
            try {
                setLoading(direction, true)

                val loadedEntries = datesBetween(startDate, endDate)
                    .map { date -> loadEntry(date) }

                _uiState.update { current ->
                    val mergedEntries = (current.entries + loadedEntries)
                        .associateBy { it.date }
                        .entries
                        .sortedBy { it.key }
                        .map { it.value }
                    current.copy(entries = mergedEntries)
                }

                if (startDate < firstLoadedDate) {
                    firstLoadedDate = startDate
                }
                if (endDate > lastLoadedDate) {
                    lastLoadedDate = endDate
                }
            } finally {
                setLoading(direction, false)
            }
        }
    }

    private suspend fun loadEntry(date: LocalDate): ReadMoreDiaryEntryUiState = try {
        val diaryContent = diaryRepository.findByDate(date)
        if (diaryContent.isBlankOrHeaderOnly) {
            ReadMoreDiaryEntryUiState.NotFound(date)
        } else {
            ReadMoreDiaryEntryUiState.Success(date = date, content = diaryContent.content)
        }
    } catch (e: Exception) {
        ReadMoreDiaryEntryUiState.Error(date = date, message = e.message ?: "Unknown error")
    }

    private fun datesBetween(startDate: LocalDate, endDate: LocalDate): List<LocalDate> = buildList {
        var current = startDate
        while (current <= endDate) {
            add(current)
            current = current.plus(DatePeriod(days = 1))
        }
    }

    private fun setLoading(direction: Direction?, isLoading: Boolean) {
        _uiState.update { current ->
            when (direction) {
                Direction.Previous -> current.copy(isLoadingPrevious = isLoading)
                Direction.Next -> current.copy(isLoadingNext = isLoading)
                null -> current.copy(isLoadingPrevious = isLoading, isLoadingNext = isLoading)
            }
        }
    }

    private enum class Direction {
        Previous,
        Next,
    }

    private companion object {
        const val INITIAL_SIDE_DAYS = 2
        const val LOAD_BATCH_SIZE = 5
    }
}