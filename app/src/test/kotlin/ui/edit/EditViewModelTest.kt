package ui.edit

import core.entity.DiaryContent
import core.repository.DiaryRepository
import core.repository.GitHubClient
import core.repository.SettingRepository
import core.time.DateProvider
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dateProvider: DateProvider

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val fixedInstant = Instant.parse("2026-01-02T12:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        dateProvider = DateProvider(fixedClock)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has today as date`() {
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            dateProvider = dateProvider
        )

        assertEquals(LocalDate(2026, 1, 2), viewModel.state.date)
    }

    @Test
    fun `initial state has empty content`() {
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            dateProvider = dateProvider
        )

        assertEquals("", viewModel.state.content)
    }

    @Test
    fun `initial state is not saving`() {
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            dateProvider = dateProvider
        )

        assertFalse(viewModel.state.isSaving)
    }

    @Test
    fun `load updates date and content`() = runTest {
        val expectedContent = "# 2026/01/15 (Thu)\n\nLoaded content"
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(LocalDate(2026, 1, 15), expectedContent)
        )
        val viewModel = EditViewModel(
            diaryRepository = fakeRepo,
            dateProvider = dateProvider
        )

        viewModel.load(LocalDate(2026, 1, 15))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LocalDate(2026, 1, 15), viewModel.state.date)
        assertEquals(expectedContent, viewModel.state.content)
    }

    @Test
    fun `updateContent changes content in state`() {
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            dateProvider = dateProvider
        )

        viewModel.updateContent("New content")

        assertEquals("New content", viewModel.state.content)
    }

    @Test
    fun `loadExisting loads content from repository`() = runTest {
        val expectedContent = "# 2026/01/02 (Thu)\n\nExisting"
        val fakeRepo = FakeDiaryRepository(
            diaryContent = DiaryContent(LocalDate(2026, 1, 2), expectedContent)
        )
        val viewModel = EditViewModel(
            diaryRepository = fakeRepo,
            dateProvider = dateProvider
        )

        var callbackCalled = false
        viewModel.loadExisting { callbackCalled = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(expectedContent, viewModel.state.content)
        assertTrue(callbackCalled)
    }

    @Test
    fun `save sets isSaving during save operation`() = runTest {
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            dateProvider = dateProvider
        )
        viewModel.updateContent("Content to save")

        viewModel.save()

        assertTrue(viewModel.state.isSaving)

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.isSaving)
    }

    @Test
    fun `save calls onSaved callback with success`() = runTest {
        val viewModel = EditViewModel(
            diaryRepository = FakeDiaryRepository(),
            dateProvider = dateProvider
        )
        viewModel.updateContent("Content")

        var savedSuccess: Boolean? = null
        viewModel.save { success, _ -> savedSuccess = success }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(savedSuccess!!)
    }
}

private class FakeDiaryRepository(
    private val diaryContent: DiaryContent = DiaryContent.init(LocalDate(2026, 1, 2))
) : DiaryRepository(
    client = FakeGitHubClient(),
    settingRepository = FakeSettingRepository()
) {
    var savedDiary: DiaryContent? = null

    override suspend fun findByDate(date: LocalDate): DiaryContent = diaryContent
    override suspend fun save(diary: DiaryContent) {
        savedDiary = diary
    }
}

private class FakeGitHubClient : GitHubClient()

private class FakeSettingRepository : SettingRepository(
    settingFilePath = kotlin.io.path.createTempFile(),
    gitHubClient = FakeGitHubClient()
)
