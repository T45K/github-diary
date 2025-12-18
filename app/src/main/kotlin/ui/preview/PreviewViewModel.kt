package ui.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.model.Result
import domain.usecase.FetchDiaryUseCase
import java.time.LocalDate

data class PreviewState(
    val date: LocalDate,
    val content: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val notFound: Boolean = false
)

class PreviewViewModel(
    private val fetchDiaryUseCase: FetchDiaryUseCase,
    initialDate: LocalDate
) {
    var state by mutableStateOf(PreviewState(date = initialDate, isLoading = true))
        private set

    suspend fun load(owner: String, repo: String, date: LocalDate) {
        state = state.copy(date = date)
        state = state.copy(isLoading = true, error = null, notFound = false)
        when (val result = fetchDiaryUseCase(owner, repo, state.date)) {
            is Result.Success -> {
                val content = result.value
                state = if (content == null) {
                    state.copy(content = null, isLoading = false, notFound = true)
                } else {
                    state.copy(content = content, isLoading = false)
                }
            }

            is Result.Failure -> {
                state = state.copy(isLoading = false, error = result.message ?: "Failed to load")
            }
        }
    }

    fun setDate(date: LocalDate) {
        state = state.copy(date = date)
    }
}
