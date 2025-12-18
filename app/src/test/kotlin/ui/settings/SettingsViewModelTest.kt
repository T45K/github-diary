package ui.settings

import core.model.Result
import data.auth.AuthMode
import data.auth.TokenStore
import data.auth.TokenValidator
import domain.usecase.ValidateTokenUseCase
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files

class SettingsViewModelTest {

    @Test
    fun `updates token and repo`() {
        val vm = buildVm(validateResult = Result.Success(true))

        vm.updateToken("abc")
        vm.updateRepo("me/repo")
        vm.updateMode(AuthMode.OAUTH)

        assertEquals("abc", vm.state.token)
        assertEquals("me/repo", vm.state.repo)
        assertEquals(AuthMode.OAUTH, vm.state.authMode)
    }

    @Test
    fun `fails on invalid token`() = runBlocking {
        val vm = buildVm(validateResult = Result.Success(false))

        vm.save()

        assertEquals("Token invalid", vm.state.message)
        assertEquals(false, vm.state.isSaving)
    }

    @Test
    fun `saves when token valid`() = runBlocking {
        val vm = buildVm(validateResult = Result.Success(true))
        vm.updateToken("ok")
        vm.updateRepo("owner/repo")

        vm.save()

        assertEquals("Saved", vm.state.message)
    }

    private fun buildVm(validateResult: Result<Boolean>): SettingsViewModel {
        val tempDir = Files.createTempDirectory("settingsVmTest")
        val tokenStore = TokenStore(filePath = tempDir.resolve("token.json"))
        val settingsRepo = data.repo.SettingsRepository(tempDir.resolve("repo.txt"))
        val validator = object : TokenValidator(HttpClient()) {
            override suspend fun validate(token: String): Result<Boolean> = validateResult
        }
        val validateUseCase = ValidateTokenUseCase(validator)
        return SettingsViewModel(tokenStore, settingsRepo, validateUseCase, CoroutineScope(Dispatchers.Unconfined))
    }
}
