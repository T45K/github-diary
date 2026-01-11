package io.github.t45k.githubDiary.ui.edit

import io.github.t45k.githubDiary.core.entity.DiaryContent
import io.github.t45k.githubDiary.core.repository.DiaryRepository
import io.github.t45k.githubDiary.core.repository.GitHubClient
import io.github.t45k.githubDiary.core.repository.SettingRepository
import io.github.t45k.githubDiary.ui.diary.edit.EditUiState
import io.github.t45k.githubDiary.ui.diary.edit.EditViewModel
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
class DiaryEditViewModelTest {

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
    fun `initial state is Loading with specified date`() {
        // given
        val date = LocalDate(2026, 1, 2)

        // when
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            date = date,
        )
        val state = viewModel.uiState.value

        // then
        assert(state is EditUiState.Loading)
        assert(state.date == LocalDate(2026, 1, 2))
    }

    @Test
    fun `load transitions to Editing with content`() = runTest {
        // given
        val date = LocalDate(2026, 1, 15)
        val expectedContent = "# 2026/01/15 (Thu)\n\nLoaded content"
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(date, expectedContent),
        )

        // when
        val viewModel = EditViewModel(
            diaryRepository = fakeRepo,
            date = date,
        )
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is EditUiState.Editing)
        val editingState = state as EditUiState.Editing
        assert(editingState.date == LocalDate(2026, 1, 15))
        assert(editingState.content == expectedContent)
        assert(editingState.isSaving == false)
    }

    @Test
    fun `updateContent changes content in Editing state`() = runTest {
        // given
        val date = LocalDate(2026, 1, 2)
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            date = date,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // when
        viewModel.updateContent("New content")
        val state = viewModel.uiState.value

        // then
        assert(state is EditUiState.Editing)
        assert((state as EditUiState.Editing).content == "New content")
    }

    @Test
    fun `save sets isSaving to true during operation`() = runTest {
        // given
        val date = LocalDate(2026, 1, 2)
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            date = date,
        )
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateContent("Content to save")

        // when
        viewModel.save()
        val state = viewModel.uiState.value

        // then
        assert(state is EditUiState.Editing)
        assert((state as EditUiState.Editing).isSaving == true)
    }

    @Test
    fun `save transitions to Saved state on success`() = runTest {
        // given
        val date = LocalDate(2026, 1, 2)
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            date = date,
        )
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateContent("Content to save")

        // when
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is EditUiState.Saved)
        assert(state.date == LocalDate(2026, 1, 2))
    }

    @Test
    fun `save calls onSaved callback with success`() = runTest {
        // given
        val date = LocalDate(2026, 1, 2)
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            date = date,
        )
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.updateContent("Content")
        var savedSuccess: Boolean? = null

        // when
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()

        // then
        assert(savedSuccess == true)
    }
}

private class FakeDiaryRepository(
    private val diaryContent: DiaryContent = DiaryContent.init(LocalDate(2026, 1, 2)),
) : DiaryRepository(
    client = FakeGitHubClient(),
    settingRepository = FakeSettingRepository(),
) {
    var savedDiary: DiaryContent? = null

    override suspend fun findByDate(date: LocalDate): DiaryContent = diaryContent
    override suspend fun save(diary: DiaryContent) {
        savedDiary = diary
    }
}

private class FakeGitHubClient : GitHubClient()

private class FakeSettingRepository : SettingRepository(
    settingFilePath = createTempFile(),
    gitHubClient = FakeGitHubClient(),
)
