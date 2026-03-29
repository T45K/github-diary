package io.github.t45k.githubDiary.search

import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.SearchCodeItem
import io.github.t45k.githubDiary.github.SearchCodeResponse
import io.github.t45k.githubDiary.github.TextMatch
import io.github.t45k.githubDiary.setting.SettingFileStorage
import io.github.t45k.githubDiary.setting.SettingRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class SearchRepositoryTest {

    @Test
    fun `search returns empty results when no credentials`() = runTest {
        // given
        val settingRepo = SearchFakeSettingRepository(loadResult = null to null)
        val client = SearchFakeGitHubClient()
        val repository = SearchRepository(client, settingRepo)

        // when
        val result = repository.search("query")

        // then
        assert(result.totalCount == 0)
        assert(result.items.isEmpty())
        assert(!result.hasMore)
    }

    @Test
    fun `search returns empty results when API returns null`() = runTest {
        // given
        val settingRepo = SearchFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo"),
        )
        val client = SearchFakeGitHubClient(searchResponse = null)
        val repository = SearchRepository(client, settingRepo)

        // when
        val result = repository.search("query")

        // then
        assert(result.totalCount == 0)
        assert(result.items.isEmpty())
        assert(!result.hasMore)
    }

    @Test
    fun `search returns parsed results with correct date extraction`() = runTest {
        // given
        val settingRepo = SearchFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo"),
        )
        val response = SearchCodeResponse(
            totalCount = 1,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(
                    name = "README.md",
                    path = "2026/01/02/README.md",
                    sha = "abc",
                    textMatches = listOf(TextMatch(fragment = "matched text")),
                ),
            ),
        )
        val client = SearchFakeGitHubClient(searchResponse = response)
        val repository = SearchRepository(client, settingRepo)

        // when
        val result = repository.search("query")

        // then
        assert(result.totalCount == 1)
        assert(result.items.size == 1)
        assert(result.items[0].path == "2026/01/02/README.md")
        assert(result.items[0].date == LocalDate(2026, 1, 2))
        assert(result.items[0].fragments == listOf("matched text"))
    }

    @Test
    fun `search returns results sorted by date descending`() = runTest {
        // given
        val settingRepo = SearchFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo"),
        )
        val response = SearchCodeResponse(
            totalCount = 3,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(name = "README.md", path = "2026/01/02/README.md", sha = "sha-1"),
                SearchCodeItem(name = "README.md", path = "2026/01/05/README.md", sha = "sha-2"),
                SearchCodeItem(name = "README.md", path = "2025/12/31/README.md", sha = "sha-3"),
            ),
        )
        val client = SearchFakeGitHubClient(searchResponse = response)
        val repository = SearchRepository(client, settingRepo)

        // when
        val result = repository.search("query")

        // then
        assert(result.items.map { it.path } == listOf(
            "2026/01/05/README.md",
            "2026/01/02/README.md",
            "2025/12/31/README.md",
        ))
    }

    @Test
    fun `search computes hasMore correctly`() = runTest {
        // given
        val settingRepo = SearchFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo"),
        )
        val response = SearchCodeResponse(
            totalCount = 50,
            incompleteResults = false,
            items = List(30) {
                SearchCodeItem(name = "README.md", path = "2026/01/$it/README.md", sha = "sha$it")
            },
        )
        val client = SearchFakeGitHubClient(searchResponse = response)
        val repository = SearchRepository(client, settingRepo)

        // when
        val result = repository.search("query", page = 1, perPage = 30)

        // then
        assert(result.hasMore)
    }

    @Test
    fun `search returns null date for goal path`() = runTest {
        // given
        val settingRepo = SearchFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo"),
        )
        val response = SearchCodeResponse(
            totalCount = 1,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(name = "README.md", path = "2026/01/README.md", sha = "abc"),
            ),
        )
        val client = SearchFakeGitHubClient(searchResponse = response)
        val repository = SearchRepository(client, settingRepo)

        // when
        val result = repository.search("query")

        // then
        assert(result.items[0].date == null)
    }
}

private class SearchFakeGitHubClient(
    private val searchResponse: SearchCodeResponse? = null,
) : GitHubClient() {
    override suspend fun searchCode(
        accessToken: GitHubPersonalAccessToken,
        query: String,
        owner: String,
        repo: String,
        page: Int,
        perPage: Int,
    ): SearchCodeResponse? = searchResponse
}

private class SearchFakeSettingRepository(
    private val loadResult: Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = null to null,
) : SettingRepository(
    fileStorage = object : SettingFileStorage {
        override suspend fun read(): String = ""
        override suspend fun write(content: String) {}
    },
    gitHubClient = object : GitHubClient() {},
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult
}
