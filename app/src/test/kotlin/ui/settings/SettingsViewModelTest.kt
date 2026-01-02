package ui.settings

import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import core.repository.GitHubClient
import core.repository.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads from repository`() = runTest {
        val fakeRepo = FakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("saved-token") to GitHubRepositoryPath("owner", "repo")
        )
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("saved-token", viewModel.state.token)
        assertEquals("owner/repo", viewModel.state.repo)
    }

    @Test
    fun `initial state has empty values when repository returns null`() = runTest {
        val fakeRepo = FakeSettingRepository(loadResult = null to null)
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("", viewModel.state.token)
        assertEquals("", viewModel.state.repo)
    }

    @Test
    fun `updateToken changes token in state`() = runTest {
        val viewModel = SettingsViewModel(FakeSettingRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateToken("new-token")

        assertEquals("new-token", viewModel.state.token)
    }

    @Test
    fun `updateRepo changes repo in state`() = runTest {
        val viewModel = SettingsViewModel(FakeSettingRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateRepo("new-owner/new-repo")

        assertEquals("new-owner/new-repo", viewModel.state.repo)
    }

    @Test
    fun `save with invalid repo format shows error`() = runTest {
        val viewModel = SettingsViewModel(FakeSettingRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateToken("token")
        viewModel.updateRepo("invalid-repo-format")

        var savedSuccess: Boolean? = null
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(savedSuccess!!)
        assertEquals("Invalid repository path format", viewModel.state.message)
    }

    @Test
    fun `save with invalid token permission shows error`() = runTest {
        val fakeRepo = FakeSettingRepository(hasPermissionResult = false)
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateToken("invalid-token")
        viewModel.updateRepo("owner/repo")

        var savedSuccess: Boolean? = null
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(savedSuccess!!)
        assertEquals("Invalid token permission", viewModel.state.message)
    }

    @Test
    fun `save with valid credentials succeeds`() = runTest {
        val fakeRepo = FakeSettingRepository(hasPermissionResult = true)
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateToken("valid-token")
        viewModel.updateRepo("owner/repo")

        var savedSuccess: Boolean? = null
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(savedSuccess!!)
        assertEquals("Saved", viewModel.state.message)
    }

    @Test
    fun `save sets isSaving during operation`() = runTest {
        val viewModel = SettingsViewModel(FakeSettingRepository(hasPermissionResult = true))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateToken("token")
        viewModel.updateRepo("owner/repo")

        viewModel.save()

        assertTrue(viewModel.state.isSaving)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.isSaving)
    }
}

private class FakeSettingRepository(
    private val loadResult: Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = null to null,
    private val hasPermissionResult: Boolean = true
) : SettingRepository(
    settingFilePath = kotlin.io.path.createTempFile(),
    gitHubClient = FakeGitHubClient()
) {
    var savedPat: GitHubPersonalAccessToken? = null
    var savedPath: GitHubRepositoryPath? = null

    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult

    override suspend fun save(pat: GitHubPersonalAccessToken?, path: GitHubRepositoryPath?) {
        savedPat = pat
        savedPath = path
    }

    override suspend fun hasPermission(pat: GitHubPersonalAccessToken, path: GitHubRepositoryPath): Boolean {
        return hasPermissionResult
    }
}

private class FakeGitHubClient : GitHubClient()
