package ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.left
import arrow.core.merge
import arrow.core.raise.either
import arrow.core.right
import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import core.repository.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SettingsUiState {
    data object Loading : SettingsUiState()

    data class Ready(
        val token: String,
        val repo: String,
        val isSaving: Boolean = false,
        val message: String? = null,
    ) : SettingsUiState()
}

class SettingsViewModel(private val settingRepository: SettingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val (accessToken, repoPath) = settingRepository.load()
            _uiState.value = SettingsUiState.Ready(
                token = accessToken?.value ?: "",
                repo = repoPath?.toString() ?: "",
            )
        }
    }

    fun updateToken(value: String) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Ready) {
            _uiState.value = currentState.copy(token = value, message = null)
        }
    }

    fun updateRepo(value: String) {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Ready) {
            _uiState.value = currentState.copy(repo = value, message = null)
        }
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        val currentState = _uiState.value
        if (currentState !is SettingsUiState.Ready) return

        _uiState.value = currentState.copy(isSaving = true, message = null)

        viewModelScope.launch {
            val (isSuccessful, message) = either {
                val repoPath = GitHubRepositoryPath(currentState.repo)
                    .mapLeft { "Invalid repository path format" }
                    .bind()
                val token = GitHubPersonalAccessToken(currentState.token).let {
                    if (settingRepository.hasPermission(it, repoPath)) {
                        it.right()
                    } else {
                        "Invalid token permission".left()
                    }
                }.bind()
                settingRepository.save(token, repoPath)
                true to "Saved"
            }.mapLeft { errorMessage ->
                false to errorMessage
            }.merge()

            _uiState.value = currentState.copy(isSaving = false, message = message)
            onSaved(isSuccessful, message)
        }
    }
}
