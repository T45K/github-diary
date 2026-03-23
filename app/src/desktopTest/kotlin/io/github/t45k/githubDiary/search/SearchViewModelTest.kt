package io.github.t45k.githubDiary.search

import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.SearchCodeItem
import io.github.t45k.githubDiary.github.SearchCodeResponse
import io.github.t45k.githubDiary.github.TextMatch
import io.github.t45k.githubDiary.setting.SettingFileStorage
import io.github.t45k.githubDiary.setting.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        // given
        val viewModel = createViewModel()

        // then
        assert(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun `search with empty query does nothing`() = runTest {
        // given
        val viewModel = createViewModel()

        // when
        viewModel.updateQuery("  ")
        viewModel.search()
        advanceUntilIdle()

        // then
        assert(viewModel.uiState.value is SearchUiState.Idle)
    }

    @Test
    fun `search transitions to Success with results`() = runTest {
        // given
        val response = SearchCodeResponse(
            totalCount = 1,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(
                    name = "README.md",
                    path = "2026/01/02/README.md",
                    sha = "abc",
                    textMatches = listOf(TextMatch(fragment = "matched")),
                ),
            ),
        )
        val viewModel = createViewModel(searchResponse = response)

        // when
        viewModel.updateQuery("test")
        viewModel.search()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assert(state is SearchUiState.Success)
        state as SearchUiState.Success
        assert(state.results.size == 1)
        assert(state.totalCount == 1)
        assert(state.query == "test")
    }

    @Test
    fun `search transitions to Success with empty results`() = runTest {
        // given
        val response = SearchCodeResponse(
            totalCount = 0,
            incompleteResults = false,
            items = emptyList(),
        )
        val viewModel = createViewModel(searchResponse = response)

        // when
        viewModel.updateQuery("nonexistent")
        viewModel.search()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assert(state is SearchUiState.Success)
        state as SearchUiState.Success
        assert(state.results.isEmpty())
        assert(state.totalCount == 0)
    }

    @Test
    fun `search transitions to Error on exception`() = runTest {
        // given
        val viewModel = createViewModel(shouldThrow = true)

        // when
        viewModel.updateQuery("test")
        viewModel.search()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assert(state is SearchUiState.Error)
    }

    @Test
    fun `loadNextPage appends results`() = runTest {
        // given
        val firstResponse = SearchCodeResponse(
            totalCount = 61,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(name = "README.md", path = "2026/01/01/README.md", sha = "a"),
            ),
        )
        val secondResponse = SearchCodeResponse(
            totalCount = 61,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(name = "README.md", path = "2026/01/02/README.md", sha = "b"),
            ),
        )
        val viewModel = createViewModel(searchResponses = listOf(firstResponse, secondResponse))

        // when
        viewModel.updateQuery("test")
        viewModel.search()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assert(state is SearchUiState.Success)
        state as SearchUiState.Success
        assert(state.results.size == 2)
        assert(state.currentPage == 2)
    }

    @Test
    fun `loadNextPage preserves results on error`() = runTest {
        // given
        val firstResponse = SearchCodeResponse(
            totalCount = 61,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(name = "README.md", path = "2026/01/01/README.md", sha = "a"),
            ),
        )
        val viewModel = createViewModel(
            searchResponses = listOf(firstResponse),
            shouldThrowOnSecondCall = true,
        )

        // when
        viewModel.updateQuery("test")
        viewModel.search()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assert(state is SearchUiState.Success)
        state as SearchUiState.Success
        assert(state.results.size == 1)
        assert(!state.hasMore)
    }

    @Test
    fun `loadNextPage does nothing when hasMore is false`() = runTest {
        // given
        val response = SearchCodeResponse(
            totalCount = 1,
            incompleteResults = false,
            items = listOf(
                SearchCodeItem(name = "README.md", path = "2026/01/01/README.md", sha = "a"),
            ),
        )
        val viewModel = createViewModel(searchResponse = response)

        // when
        viewModel.updateQuery("test")
        viewModel.search()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // then
        val state = viewModel.uiState.value
        assert(state is SearchUiState.Success)
        state as SearchUiState.Success
        assert(state.results.size == 1)
        assert(state.currentPage == 1)
    }

    private fun createViewModel(
        searchResponse: SearchCodeResponse? = null,
        searchResponses: List<SearchCodeResponse>? = null,
        shouldThrow: Boolean = false,
        shouldThrowOnSecondCall: Boolean = false,
    ): SearchViewModel {
        val fakeClient = object : GitHubClient() {
            var callCount = 0
            override suspend fun searchCode(
                accessToken: GitHubPersonalAccessToken,
                query: String,
                owner: String,
                repo: String,
                page: Int,
                perPage: Int,
            ): SearchCodeResponse? {
                if (shouldThrow) throw RuntimeException("Search failed")
                val currentCall = callCount++
                if (shouldThrowOnSecondCall && currentCall > 0) throw RuntimeException("Search failed")
                val responses = searchResponses ?: listOfNotNull(searchResponse)
                return responses.getOrNull(currentCall)
            }
        }
        val fakeSettingRepo = object : SettingRepository(
            fileStorage = object : SettingFileStorage {
                override suspend fun read(): String = ""
                override suspend fun write(content: String) {}
            },
            gitHubClient = object : GitHubClient() {},
        ) {
            override suspend fun load() = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        }
        return SearchViewModel(SearchRepository(fakeClient, fakeSettingRepo))
    }
}
