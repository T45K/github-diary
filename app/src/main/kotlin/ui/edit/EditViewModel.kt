package ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.entity.DiaryContent
import core.repository.DiaryRepository
import core.time.DateProvider
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class EditState(
    val date: LocalDate,
    val content: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

class EditViewModel(
    private val diaryRepository: DiaryRepository,
    dateProvider: DateProvider,
) : ViewModel() {
    var state by mutableStateOf(EditState(date = dateProvider.today()))
        private set

    fun load(date: LocalDate) {
        state = state.copy(date = date)
        viewModelScope.launch {
            val diaryContent = diaryRepository.findByDate(date)
            state = state.copy(content = diaryContent.content)
        }
    }

    fun updateContent(value: String) {
        state = state.copy(content = value)
    }

    fun loadExisting(onLoaded: () -> Unit = {}) {
        viewModelScope.launch {
            val diaryContent = diaryRepository.findByDate(state.date)
            updateContent(diaryContent.content)

            state = state.copy(error = null)

            onLoaded()
        }
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        state = state.copy(isSaving = true, error = null)
        viewModelScope.launch {
            diaryRepository.save(DiaryContent(state.date, state.content))
            state = state.copy(isSaving = false)
            onSaved(true, state.error)
        }
    }
}
