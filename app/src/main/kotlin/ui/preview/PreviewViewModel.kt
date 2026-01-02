package ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.entity.DiaryContent
import core.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

sealed class PreviewUiState {
    abstract val date: LocalDate

    data class Loading(override val date: LocalDate) : PreviewUiState()

    data class Success(
        override val date: LocalDate,
        val content: String,
    ) : PreviewUiState()

    data class NotFound(override val date: LocalDate) : PreviewUiState()

    data class Error(
        override val date: LocalDate,
        val message: String,
    ) : PreviewUiState()
}

class PreviewViewModel(
    private val diaryRepository: DiaryRepository,
    initialDate: LocalDate,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PreviewUiState>(PreviewUiState.Loading(initialDate))
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    fun load(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = PreviewUiState.Loading(date)

            try {
                val diaryContent = diaryRepository.findByDate(date)
                val headerOnly = DiaryContent.init(date).content
                if (diaryContent.content.isBlank() || diaryContent.content == headerOnly) {
                    _uiState.value = PreviewUiState.NotFound(date)
                } else {
                    _uiState.value = PreviewUiState.Success(
                        date = date,
                        content = diaryContent.content
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PreviewUiState.Error(
                    date = date,
                    message = e.message ?: "Unknown error"
                )
            }
        }
    }
}
