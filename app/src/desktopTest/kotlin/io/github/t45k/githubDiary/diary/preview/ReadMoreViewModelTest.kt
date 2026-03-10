package io.github.t45k.githubDiary.diary.preview

import io.github.t45k.githubDiary.diary.DiaryContent
import io.github.t45k.githubDiary.diary.DiaryRepository
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.setting.SettingFileStorage
import io.github.t45k.githubDiary.setting.SettingRepository
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
class ReadMoreViewModelTest {
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
    fun `initial load fetches five days around the target date`() = runTest {
        val targetDate = LocalDate(2026, 1, 10)
        val repository = RangeDiaryRepository()

        val viewModel = ReadMoreViewModel(repository, targetDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assert(state.entries.map { it.date } == listOf(
            LocalDate(2026, 1, 8),
            LocalDate(2026, 1, 9),
            LocalDate(2026, 1, 10),
            LocalDate(2026, 1, 11),
            LocalDate(2026, 1, 12),
        ))
    }

    @Test
    fun `loadPrevious prepends older diaries without duplicates`() = runTest {
        val targetDate = LocalDate(2026, 1, 10)
        val repository = RangeDiaryRepository()

        val viewModel = ReadMoreViewModel(repository, targetDate)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadPrevious()
        testDispatcher.scheduler.advanceUntilIdle()

        val dates = viewModel.uiState.value.entries.map { it.date }
        assert(dates.first() == LocalDate(2026, 1, 3))
        assert(dates.last() == LocalDate(2026, 1, 12))
        assert(dates.distinct().size == dates.size)
    }

    @Test
    fun `loadNext appends newer diaries`() = runTest {
        val targetDate = LocalDate(2026, 1, 10)
        val repository = RangeDiaryRepository()

        val viewModel = ReadMoreViewModel(repository, targetDate)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadNext()
        testDispatcher.scheduler.advanceUntilIdle()

        val dates = viewModel.uiState.value.entries.map { it.date }
        assert(dates.first() == LocalDate(2026, 1, 8))
        assert(dates.last() == LocalDate(2026, 1, 17))
    }
}

private class RangeDiaryRepository : DiaryRepository(
    client = FakeGitHubClientForReadMore(),
    settingRepository = FakeSettingRepositoryForReadMore(),
) {
    override suspend fun findByDate(date: LocalDate): DiaryContent = DiaryContent(date, "# ${date}\n\n$contentSuffix")

    override suspend fun save(diary: DiaryContent) {}

    private companion object {
        const val contentSuffix = "Diary"
    }
}

private class FakeGitHubClientForReadMore : GitHubClient()

private class FakeSettingRepositoryForReadMore : SettingRepository(
    fileStorage = object : SettingFileStorage {
        override suspend fun read(): String = ""
        override suspend fun write(content: String) {}
    },
    gitHubClient = FakeGitHubClientForReadMore(),
)