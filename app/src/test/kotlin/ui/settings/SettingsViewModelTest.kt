package ui.settings

import core.model.Result
import data.auth.AuthMode
import data.auth.TokenStore
import data.auth.TokenValidator
import domain.usecase.ValidateTokenUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

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
    fun `fails on invalid token`() = runTest {
        val vm = buildVm(validateResult = Result.Success(false))

        vm.save()

        assertEquals("Token invalid", vm.state.message)
        assertEquals(false, vm.state.isSaving)
    }

    @Test
    fun `saves when token valid`() = runTest {
        val vm = buildVm(validateResult = Result.Success(true))
        vm.updateToken("ok")

        vm.save()

        assertEquals("Saved", vm.state.message)
    }

    private fun buildVm(validateResult: Result<Boolean>): SettingsViewModel {
        val tempDir = Files.createTempDirectory("settingsVmTest")
        val tokenStore = TokenStore(filePath = tempDir.resolve("token.json"))
        val validator = object : TokenValidator(HttpClient(MockEngine { respond("", HttpStatusCode.OK) })) {
            override suspend fun validate(token: String): Result<Boolean> = validateResult
        }
        val validateUseCase = ValidateTokenUseCase(validator)
        return SettingsViewModel(tokenStore, validateUseCase, CoroutineScope(Dispatchers.Unconfined))
    }
}