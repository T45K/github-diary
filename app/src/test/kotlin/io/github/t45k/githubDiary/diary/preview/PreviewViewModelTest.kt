package io.github.t45k.githubDiary.diary.preview

import io.github.t45k.githubDiary.diary.DiaryContent
import io.github.t45k.githubDiary.diary.DiaryRepository
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.setting.SettingRepository
import kotlin.io.path.createTempFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
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
        // given
        val date = LocalDate(2026, 1, 2)

        // when
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(),
            date = date
        )
        val state = viewModel.uiState.value

        // then
        assert(state is PreviewUiState.Loading)
        assert(state.date == date)
    }

    @Test
    fun `load transitions to Success with content`() = runTest {
        // given
        val date = LocalDate(2026, 1, 2)
        val expectedContent = "# 2026/01/02 (Fri)\n\nTest diary content"
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(date, expectedContent)
        )

        // when
        val viewModel = PreviewViewModel(
            diaryRepository = fakeRepo,
            date = date
        )
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is PreviewUiState.Success)
        val successState = state as PreviewUiState.Success
        assert(successState.content == expectedContent)
        assert(successState.date == LocalDate(2026, 1, 2))
    }

    @Test
    fun `load transitions to NotFound when content is empty`() = runTest {
        // given
        val date = LocalDate(2026, 1, 2)
        val headerOnly = DiaryContent.init(date).content
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(date, headerOnly)
        )

        // when
        val viewModel = PreviewViewModel(
            diaryRepository = fakeRepo,
            date = date
        )
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is PreviewUiState.NotFound)
        assert(state.date == date)
    }

    @Test
    fun `load sets correct date in state`() = runTest {
        // given
        val date = LocalDate(2026, 1, 15)
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(date, "# 2026/01/15 (Thu)\n\nContent")
        )

        // when
        val viewModel = PreviewViewModel(
            diaryRepository = fakeRepo,
            date = date
        )
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state.date == LocalDate(2026, 1, 15))
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
    settingFilePath = createTempFile(),
    gitHubClient = FakeGitHubClient()
)
