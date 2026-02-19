package io.github.t45k.githubDiary.diary.edit

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.t45k.githubDiary.diary.DiaryContent
import io.github.t45k.githubDiary.diary.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

sealed class EditUiState {
    abstract val date: LocalDate

    data class Loading(override val date: LocalDate) : EditUiState()

    data class Editing(
        override val date: LocalDate,
        val content: String,
        val isSaving: Boolean = false,
    ) : EditUiState()

    data class Saved(override val date: LocalDate) : EditUiState()

    data class Error(
        override val date: LocalDate,
        val content: String,
        val message: String,
    ) : EditUiState()
}

class EditViewModel(
    private val diaryRepository: DiaryRepository,
    date: LocalDate,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading(date))
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    init {
        load(date)
    }

    private fun load(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = EditUiState.Loading(date)

            try {
                val diaryContent = diaryRepository.findByDate(date)
                _uiState.value = EditUiState.Editing(
                    date = date,
                    content = diaryContent.content,
                )
            } catch (e: Exception) {
                _uiState.value = EditUiState.Error(
                    date = date,
                    content = "",
                    message = e.message ?: "Failed to load",
                )
            }
        }
    }

    fun updateContent(value: TextFieldValue) {
        val currentState = _uiState.value
        if (currentState is EditUiState.Editing) {
            _uiState.value = currentState.copy(content = value.text)
        } else if (currentState is EditUiState.Error) {
            _uiState.value = EditUiState.Editing(
                date = currentState.date,
                content = value.text,
            )
        }
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        val currentState = _uiState.value
        if (currentState !is EditUiState.Editing) return

        _uiState.value = currentState.copy(isSaving = true)

        viewModelScope.launch {
            try {
                diaryRepository.save(DiaryContent(currentState.date, currentState.content))
                _uiState.value = EditUiState.Saved(currentState.date)
                onSaved(true, null)
            } catch (e: Exception) {
                _uiState.value = EditUiState.Error(
                    date = currentState.date,
                    content = currentState.content,
                    message = e.message ?: "Failed to save",
                )
                onSaved(false, e.message)
            }
        }
    }
}
