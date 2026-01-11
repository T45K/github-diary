package io.github.t45k.githubDiary.core.repository

import io.github.t45k.githubDiary.core.entity.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.core.entity.GitHubRepositoryPath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SettingRepositoryTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `load returns null when file is empty`() = runTest {
        // given
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        // when
        val (pat, path) = repository.load()

        // then
        assert(pat == null)
        assert(path == null)
    }

    @Test
    fun `save and load round trip`() = runTest {
        // given
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)
        val pat = GitHubPersonalAccessToken("ghp_testtoken")
        val path = GitHubRepositoryPath("owner", "repo")

        // when
        repository.save(pat, path)
        val (loadedPat, loadedPath) = repository.load()

        // then
        assert(loadedPat?.value == pat.value)
        assert(loadedPath?.owner == path.owner)
        assert(loadedPath?.name == path.name)
    }

    @Test
    fun `save with null values`() = runTest {
        // given
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        // when
        repository.save(null, null)
        val (loadedPat, loadedPath) = repository.load()

        // then
        assert(loadedPat == null)
        assert(loadedPath == null)
    }

    @Test
    fun `hasPermission returns true when getRepository succeeds`() = runTest {
        // given
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient(repositoryExists = true)
        val repository = SettingRepository(settingsFile, gitHubClient)
        val pat = GitHubPersonalAccessToken("token")
        val path = GitHubRepositoryPath("owner", "repo")

        // when
        val result = repository.hasPermission(pat, path)

        // then
        assert(result == true)
    }

    @Test
    fun `hasPermission returns false when getRepository fails`() = runTest {
        // given
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient(repositoryExists = false)
        val repository = SettingRepository(settingsFile, gitHubClient)
        val pat = GitHubPersonalAccessToken("token")
        val path = GitHubRepositoryPath("owner", "repo")

        // when
        val result = repository.hasPermission(pat, path)

        // then
        assert(result == false)
    }

    @Test
    fun `load handles corrupted file gracefully`() = runTest {
        // given
        val settingsFile = tempDir.resolve("settings.json")
        settingsFile.toFile().writeText("not valid json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        // when
        val (pat, path) = repository.load()

        // then
        assert(pat == null)
        assert(path == null)
    }
}

private class SettingRepoFakeGitHubClient(
    private val repositoryExists: Boolean = true
) : GitHubClient() {
    override suspend fun getRepository(
        accessToken: GitHubPersonalAccessToken,
        owner: String,
        repo: String
    ): GetRepositoryResponse? {
        return if (repositoryExists) GetRepositoryResponse(12345) else null
    }
}
