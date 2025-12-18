package ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import core.model.Result
import data.auth.AuthMode
import data.auth.TokenStore
import data.repo.SettingsRepository
import domain.usecase.ValidateTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SettingsState(
    val token: String = "",
    val authMode: AuthMode = AuthMode.PAT,
    val repo: String = "",
    val isSaving: Boolean = false,
    val message: String? = null
)

class SettingsViewModel(
    private val tokenStore: TokenStore,
    private val settingsRepository: SettingsRepository,
    private val validateToken: ValidateTokenUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var state by mutableStateOf(SettingsState())
        private set

    init {
        // ロード済み設定の復元（平文保存前提）
        when (val loadedRepo = settingsRepository.load()) {
            is Result.Success -> loadedRepo.value?.let { state = state.copy(repo = it) }
            else -> {}
        }
        when (val loadedToken = tokenStore.load()) {
            is Result.Success -> loadedToken.value?.let {
                state = state.copy(token = it.token, authMode = it.mode)
            }

            else -> {}
        }
    }

    fun updateToken(value: String) {
        state = state.copy(token = value)
    }

    fun updateRepo(value: String) {
        state = state.copy(repo = value)
    }

    fun updateMode(mode: AuthMode) {
        state = state.copy(authMode = mode)
    }

    fun save(onSaved: (Boolean, String?) -> Unit = { _, _ -> }) {
        state = state.copy(isSaving = true, message = null)
        scope.launch {
            val validation = validateToken(state.token)
            when (validation) {
                is Result.Success -> {
                    if (!validation.value) {
                        state = state.copy(isSaving = false, message = "Token invalid")
                        onSaved(false, state.message)
                        return@launch
                    }
                }

                is Result.Failure -> {
                    state = state.copy(isSaving = false, message = validation.message ?: "Validation failed")
                    onSaved(false, state.message)
                    return@launch
                }
            }
            val tokenSave = tokenStore.save(TokenStore.StoredToken(state.token, state.authMode))
            val repoSave = settingsRepository.save(state.repo)
            val success = tokenSave is Result.Success && repoSave is Result.Success
            val message = when {
                success -> "Saved"
                tokenSave is Result.Failure -> tokenSave.message ?: "Save failed"
                repoSave is Result.Failure -> repoSave.message ?: "Save failed"
                else -> "Save failed"
            }
            state = state.copy(isSaving = false, message = message)
            onSaved(success, state.message)
        }
    }
}
