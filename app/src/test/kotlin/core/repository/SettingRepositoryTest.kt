package core.repository

import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class SettingRepositoryTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `load returns null when file is empty`() = runTest {
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        val (pat, path) = repository.load()

        assertNull(pat)
        assertNull(path)
    }

    @Test
    fun `save and load round trip`() = runTest {
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        val pat = GitHubPersonalAccessToken("ghp_testtoken")
        val path = GitHubRepositoryPath("owner", "repo")

        repository.save(pat, path)
        val (loadedPat, loadedPath) = repository.load()

        assertEquals(pat.value, loadedPat?.value)
        assertEquals(path.owner, loadedPath?.owner)
        assertEquals(path.name, loadedPath?.name)
    }

    @Test
    fun `save with null values`() = runTest {
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        repository.save(null, null)
        val (loadedPat, loadedPath) = repository.load()

        assertNull(loadedPat)
        assertNull(loadedPath)
    }

    @Test
    fun `hasPermission returns true when getRepository succeeds`() = runTest {
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient(repositoryExists = true)
        val repository = SettingRepository(settingsFile, gitHubClient)

        val pat = GitHubPersonalAccessToken("token")
        val path = GitHubRepositoryPath("owner", "repo")

        assertTrue(repository.hasPermission(pat, path))
    }

    @Test
    fun `hasPermission returns false when getRepository fails`() = runTest {
        val settingsFile = tempDir.resolve("settings.json")
        val gitHubClient = SettingRepoFakeGitHubClient(repositoryExists = false)
        val repository = SettingRepository(settingsFile, gitHubClient)

        val pat = GitHubPersonalAccessToken("token")
        val path = GitHubRepositoryPath("owner", "repo")

        assertFalse(repository.hasPermission(pat, path))
    }

    @Test
    fun `load handles corrupted file gracefully`() = runTest {
        val settingsFile = tempDir.resolve("settings.json")
        settingsFile.toFile().writeText("not valid json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(settingsFile, gitHubClient)

        val (pat, path) = repository.load()

        assertNull(pat)
        assertNull(path)
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
