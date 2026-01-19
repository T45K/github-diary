package io.github.t45k.githubDiary.setting

import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.GitHubClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {

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
    fun `initial state is Loading`() {
        // given & when
        val viewModel = SettingsViewModel(FakeSettingRepository())

        // then
        assert(viewModel.uiState.value is SettingsUiState.Loading)
    }

    @Test
    fun `state becomes Ready with values from repository`() = runTest {
        // given
        val fakeRepo = FakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("saved-token") to GitHubRepositoryPath("owner", "repo")
        )

        // when
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is SettingsUiState.Ready)
        val readyState = state as SettingsUiState.Ready
        assert(readyState.token == "saved-token")
        assert(readyState.repo == "owner/repo")
    }

    @Test
    fun `state becomes Ready with empty values when repository returns null`() = runTest {
        // given
        val fakeRepo = FakeSettingRepository(loadResult = null to null)

        // when
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is SettingsUiState.Ready)
        val readyState = state as SettingsUiState.Ready
        assert(readyState.token == "")
        assert(readyState.repo == "")
    }

    @Test
    fun `updateToken changes token in Ready state`() = runTest {
        // given
        val viewModel = SettingsViewModel(FakeSettingRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        // when
        viewModel.updateToken("new-token")
        val state = viewModel.uiState.value

        // then
        assert(state is SettingsUiState.Ready)
        assert((state as SettingsUiState.Ready).token == "new-token")
    }

    @Test
    fun `updateRepo changes repo in Ready state`() = runTest {
        // given
        val viewModel = SettingsViewModel(FakeSettingRepository())
        testDispatcher.scheduler.advanceUntilIdle()

        // when
        viewModel.updateRepo("new-owner/new-repo")
        val state = viewModel.uiState.value

        // then
        assert(state is SettingsUiState.Ready)
        assert((state as SettingsUiState.Ready).repo == "new-owner/new-repo")
    }

    @Test
    fun `save with invalid repo format shows error`() = runTest {
        // given
        val viewModel = SettingsViewModel(FakeSettingRepository())
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateToken("token")
        viewModel.updateRepo("invalid-repo-format")
        var savedSuccess: Boolean? = null

        // when
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(savedSuccess == false)
        assert(state is SettingsUiState.Ready)
        assert((state as SettingsUiState.Ready).message == "Invalid repository path format")
    }

    @Test
    fun `save with invalid token permission shows error`() = runTest {
        // given
        val fakeRepo = FakeSettingRepository(hasPermissionResult = false)
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateToken("invalid-token")
        viewModel.updateRepo("owner/repo")
        var savedSuccess: Boolean? = null

        // when
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(savedSuccess == false)
        assert(state is SettingsUiState.Ready)
        assert((state as SettingsUiState.Ready).message == "Invalid token permission")
    }

    @Test
    fun `save with valid credentials succeeds`() = runTest {
        // given
        val fakeRepo = FakeSettingRepository(hasPermissionResult = true)
        val viewModel = SettingsViewModel(fakeRepo)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateToken("valid-token")
        viewModel.updateRepo("owner/repo")
        var savedSuccess: Boolean? = null

        // when
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(savedSuccess == true)
        assert(state is SettingsUiState.Ready)
        assert((state as SettingsUiState.Ready).message == "Saved")
    }

    @Test
    fun `save sets isSaving during operation`() = runTest {
        // given
        val viewModel = SettingsViewModel(FakeSettingRepository(hasPermissionResult = true))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateToken("token")
        viewModel.updateRepo("owner/repo")

        // when
        viewModel.save()
        val stateDuring = viewModel.uiState.value

        // then
        assert(stateDuring is SettingsUiState.Ready)
        assert((stateDuring as SettingsUiState.Ready).isSaving == true)

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val stateAfter = viewModel.uiState.value

        // then
        assert(stateAfter is SettingsUiState.Ready)
        assert((stateAfter as SettingsUiState.Ready).isSaving == false)
    }
}

private class FakeSettingRepository(
    private val loadResult: Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = null to null,
    private val hasPermissionResult: Boolean = true
) : SettingRepository(
    fileStorage = object : SettingFileStorage {
        override suspend fun read(): String = ""
        override suspend fun write(content: String) {}
    },
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
