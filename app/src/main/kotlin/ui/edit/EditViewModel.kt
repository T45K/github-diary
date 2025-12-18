package ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.model.Result
import core.time.DateFormatter
import data.repo.DiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

data class EditState(
    val date: LocalDate,
    val content: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

class EditViewModel(
    private val diaryRepository: DiaryRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private var owner: String = ""
    private var repo: String = ""
    private var initialDate: LocalDate = LocalDate.now()
    fun setContext(owner: String, repo: String, date: LocalDate) {
        this.owner = owner
        this.repo = repo
        this.initialDate = date
        state = EditState(date = date, content = "# ${DateFormatter.formatDisplay(date)}")
    }

    var state by mutableStateOf(EditState(date = initialDate, content = "# ${DateFormatter.formatDisplay(initialDate)}"))
        private set

    fun updateContent(value: String) {
        state = state.copy(content = value)
    }

    fun loadExisting(onLoaded: () -> Unit = {}) {
        scope.launch {
            when (val result = diaryRepository.getDiary(owner, repo, state.date)) {
                is Result.Success -> {
                    result.value?.let { updateContent(it) }
                    state = state.copy(error = null)
                }

                is Result.Failure -> state = state.copy(error = result.message)
            }
            onLoaded()
        }
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        state = state.copy(isSaving = true, error = null)
        scope.launch {
            val result = diaryRepository.saveDiary(owner, repo, state.date, state.content)
            state = when (result) {
                is Result.Success -> state.copy(isSaving = false)
                is Result.Failure -> state.copy(isSaving = false, error = result.message)
            }
            onSaved(result is Result.Success, state.error)
        }
    }
}
