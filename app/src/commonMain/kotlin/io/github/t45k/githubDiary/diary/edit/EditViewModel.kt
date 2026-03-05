package io.github.t45k.githubDiary.diary.edit

import androidx.compose.foundation.text.input.TextFieldState
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
        val isSaving: Boolean = false,
    ) : EditUiState()

    data class Saved(override val date: LocalDate) : EditUiState()

    data class Error(
        override val date: LocalDate,
        val message: String,
    ) : EditUiState()
}

class EditViewModel(
    private val diaryRepository: DiaryRepository,
    date: LocalDate,
) : ViewModel() {
    private val _uiState = MutableStateFlow<EditUiState>(EditUiState.Loading(date))
    val uiState: StateFlow<EditUiState> = _uiState.asStateFlow()

    val textFieldState = TextFieldState()

    init {
        load(date)
    }

    private fun load(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = EditUiState.Loading(date)

            try {
                val diaryContent = diaryRepository.findByDate(date)
                textFieldState.edit { replace(0, length, diaryContent.content) }
                _uiState.value = EditUiState.Editing(date = date)
            } catch (e: Exception) {
                _uiState.value = EditUiState.Error(
                    date = date,
                    message = e.message ?: "Failed to load",
                )
            }
        }
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        val currentState = _uiState.value
        val date = when (currentState) {
            is EditUiState.Editing -> currentState.date
            is EditUiState.Error -> currentState.date
            else -> return
        }

        _uiState.value = EditUiState.Editing(date = date, isSaving = true)
        val content = textFieldState.text.toString()

        viewModelScope.launch {
            try {
                diaryRepository.save(DiaryContent(date, content))
                _uiState.value = EditUiState.Saved(date)
                onSaved(true, null)
            } catch (e: Exception) {
                _uiState.value = EditUiState.Error(
                    date = date,
                    message = e.message ?: "Failed to save",
                )
                onSaved(false, e.message)
            }
        }
    }
}
