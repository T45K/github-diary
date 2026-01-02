package ui.preview

import core.entity.DiaryContent
import core.repository.DiaryRepository
import core.repository.GitHubClient
import core.repository.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreviewViewModelTest {

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
    fun `initial state is Loading with provided date`() {
        val date = LocalDate(2026, 1, 2)
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(),
            initialDate = date
        )

        val state = viewModel.uiState.value
        assertTrue(state is PreviewUiState.Loading)
        assertEquals(date, state.date)
    }

    @Test
    fun `load transitions to Success with content`() = runTest {
        val expectedContent = "# 2026/01/02 (Fri)\n\nTest diary content"
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(LocalDate(2026, 1, 2), expectedContent)
        )
        val viewModel = PreviewViewModel(
            diaryRepository = fakeRepo,
            initialDate = LocalDate(2026, 1, 2)
        )

        viewModel.load(LocalDate(2026, 1, 2))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PreviewUiState.Success)
        val successState = state as PreviewUiState.Success
        assertEquals(expectedContent, successState.content)
        assertEquals(LocalDate(2026, 1, 2), successState.date)
    }

    @Test
    fun `load transitions to NotFound when content is empty`() = runTest {
        val date = LocalDate(2026, 1, 2)
        val headerOnly = DiaryContent.init(date).content
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(date, headerOnly)
        )
        val viewModel = PreviewViewModel(
            diaryRepository = fakeRepo,
            initialDate = date
        )

        viewModel.load(date)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PreviewUiState.NotFound)
        assertEquals(date, state.date)
    }

    @Test
    fun `load updates date correctly`() = runTest {
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(
                diaryContent = DiaryContent(LocalDate(2026, 1, 15), "# 2026/01/15 (Thu)\n\nContent")
            ),
            initialDate = LocalDate(2026, 1, 1)
        )

        viewModel.load(LocalDate(2026, 1, 15))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LocalDate(2026, 1, 15), state.date)
    }
}

private class FakeDiaryRepository(
    private val diaryContent: DiaryContent = DiaryContent.init(LocalDate(2026, 1, 1))
) : DiaryRepository(
    client = FakeGitHubClient(),
    settingRepository = FakeSettingRepository()
) {
    override suspend fun findByDate(date: LocalDate): DiaryContent = diaryContent
    override suspend fun save(diary: DiaryContent) {}
}

private class FakeGitHubClient : GitHubClient()

private class FakeSettingRepository : SettingRepository(
    settingFilePath = kotlin.io.path.createTempFile(),
    gitHubClient = FakeGitHubClient()
)
