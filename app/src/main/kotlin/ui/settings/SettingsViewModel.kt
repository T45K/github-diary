package ui.settings

import core.model.Result
import data.auth.AuthMode
import data.auth.TokenStore
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
    private val validateToken: ValidateTokenUseCase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    var state: SettingsState = SettingsState()
        private set

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
            val saveResult = tokenStore.save(TokenStore.StoredToken(state.token, state.authMode))
            state = when (saveResult) {
                is Result.Success -> state.copy(isSaving = false, message = "Saved")
                is Result.Failure -> state.copy(isSaving = false, message = saveResult.message ?: "Save failed")
            }
            onSaved(saveResult is Result.Success, state.message)
        }
    }
}