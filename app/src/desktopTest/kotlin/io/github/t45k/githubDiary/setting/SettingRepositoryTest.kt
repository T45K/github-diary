package io.github.t45k.githubDiary.setting

import io.github.t45k.githubDiary.github.GetRepositoryResponse
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SettingRepositoryTest {

    @Test
    fun `load returns null when file is empty`() = runTest {
        // given
        val fileStorage = FakeSettingFileStorage()
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(fileStorage, gitHubClient)

        // when
        val (pat, path) = repository.load()

        // then
        assert(pat == null)
        assert(path == null)
    }

    @Test
    fun `save and load round trip`() = runTest {
        // given
        val fileStorage = FakeSettingFileStorage()
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(fileStorage, gitHubClient)
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
        val fileStorage = FakeSettingFileStorage()
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(fileStorage, gitHubClient)

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
        val fileStorage = FakeSettingFileStorage()
        val gitHubClient = SettingRepoFakeGitHubClient(repositoryExists = true)
        val repository = SettingRepository(fileStorage, gitHubClient)
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
        val fileStorage = FakeSettingFileStorage()
        val gitHubClient = SettingRepoFakeGitHubClient(repositoryExists = false)
        val repository = SettingRepository(fileStorage, gitHubClient)
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
        val fileStorage = FakeSettingFileStorage("not valid json")
        val gitHubClient = SettingRepoFakeGitHubClient()
        val repository = SettingRepository(fileStorage, gitHubClient)

        // when
        val (pat, path) = repository.load()

        // then
        assert(pat == null)
        assert(path == null)
    }
}

private class FakeSettingFileStorage(
    private var content: String = ""
) : SettingFileStorage {
    override suspend fun read(): String = content
    override suspend fun write(content: String) {
        this.content = content
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
