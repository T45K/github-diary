package ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.left
import arrow.core.merge
import arrow.core.raise.either
import arrow.core.right
import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import core.repository.SettingRepository
import kotlinx.coroutines.launch

data class SettingsState(
    val token: String = "",
    val repo: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
)

class SettingsViewModel(private val settingRepository: SettingRepository) : ViewModel() {
    var state by mutableStateOf(SettingsState())
        private set

    init {
        viewModelScope.launch {
            val (accessToken, repoPath) = settingRepository.load()
            state = state.copy(
                token = accessToken?.value ?: "",
                repo = repoPath?.toString() ?: "",
            )
        }
    }

    fun updateToken(value: String) {
        state = state.copy(token = value)
    }

    fun updateRepo(value: String) {
        state = state.copy(repo = value)
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        state = state.copy(isSaving = true, message = null)
        viewModelScope.launch {
            val (isSuccessful, message) = either {
                val repoPath = GitHubRepositoryPath(state.repo).mapLeft { "Invalid repository path format" }.bind()
                val token = GitHubPersonalAccessToken(state.token).let {
                    if (settingRepository.hasPermission(it, repoPath)) {
                        it.right()
                    } else {
                        "Invalid token permission".left()
                    }
                }.bind()
                settingRepository.save(token, repoPath)

                val isSuccessful = true
                val successMessage = "Saved"
                isSuccessful to successMessage
            }.mapLeft { errorMessage ->
                val isSuccessful = false
                isSuccessful to errorMessage
            }.merge()

            state = state.copy(isSaving = false, message = message)
            onSaved(isSuccessful, message)
        }
    }
}
