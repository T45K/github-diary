package ui.preview

import core.entity.DiaryContent
import core.repository.DiaryRepository
import core.repository.GitHubClient
import core.repository.SettingRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreviewViewModelTest {

    @Test
    fun `initial state has provided date`() {
        val date = LocalDate(2026, 1, 2)
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(),
            initialDate = date
        )

        assertEquals(date, viewModel.state.date)
    }

    @Test
    fun `initial state is loading`() {
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(),
            initialDate = LocalDate(2026, 1, 2)
        )

        assertTrue(viewModel.state.isLoading)
    }

    @Test
    fun `load updates content from repository`() = runTest {
        val expectedContent = "# 2026/01/02 (Thu)\n\nTest diary content"
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(LocalDate(2026, 1, 2), expectedContent)
        )
        val viewModel = PreviewViewModel(
            diaryRepository = fakeRepo,
            initialDate = LocalDate(2026, 1, 2)
        )

        viewModel.load(LocalDate(2026, 1, 2))

        assertEquals(expectedContent, viewModel.state.content)
        assertFalse(viewModel.state.isLoading)
    }

    @Test
    fun `load updates date`() = runTest {
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(),
            initialDate = LocalDate(2026, 1, 1)
        )

        viewModel.load(LocalDate(2026, 1, 15))

        assertEquals(LocalDate(2026, 1, 15), viewModel.state.date)
    }

    @Test
    fun `setDate updates only date without loading content`() {
        val viewModel = PreviewViewModel(
            diaryRepository = FakeDiaryRepository(),
            initialDate = LocalDate(2026, 1, 1)
        )

        viewModel.setDate(LocalDate(2026, 2, 28))

        assertEquals(LocalDate(2026, 2, 28), viewModel.state.date)
        assertTrue(viewModel.state.isLoading)
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
