package ui.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.repository.DiaryRepository
import kotlinx.datetime.LocalDate

data class PreviewState(
    val date: LocalDate,
    val content: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val notFound: Boolean = false,
)

class PreviewViewModel(
    private val diaryRepository: DiaryRepository,
    initialDate: LocalDate,
) {
    var state by mutableStateOf(PreviewState(date = initialDate, isLoading = true))
        private set

    suspend fun load(date: LocalDate) {
        state = state.copy(date = date)
        state = state.copy(isLoading = true, error = null, notFound = false)
        val diaryContent = diaryRepository.findByDate(state.date)
        state = state.copy(content = diaryContent.content, isLoading = false)
    }

    fun setDate(date: LocalDate) {
        state = state.copy(date = date)
    }
}
