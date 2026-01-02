package ui.calendar

import core.entity.Calendar
import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import core.repository.CalendarRepository
import core.repository.GitHubClient
import core.repository.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

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
    fun `initial state has provided year and month`() {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 1
        )

        assertEquals(2026, viewModel.state.year)
        assertEquals(1, viewModel.state.month)
    }

    @Test
    fun `initial state has empty days`() {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 1
        )

        assertTrue(viewModel.state.days.isEmpty())
    }

    @Test
    fun `load sets isLoading to true then false`() = runTest {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 1
        )

        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.state.isLoading)
    }

    @Test
    fun `load populates days from repository`() = runTest {
        val fakeRepo = FakeCalendarRepository(
            returnCalendar = Calendar(
                listOf(
                    LocalDate(2026, 1, 1) to true,
                    LocalDate(2026, 1, 2) to false
                )
            )
        )
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            initialYear = 2026,
            initialMonth = 1
        )

        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.state.days.size)
        assertTrue(viewModel.state.days[0].exists)
        assertFalse(viewModel.state.days[1].exists)
    }

    @Test
    fun `load sets error when calendar is empty`() = runTest {
        val fakeRepo = FakeCalendarRepository(returnCalendar = Calendar(emptyList()))
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            initialYear = 2026,
            initialMonth = 1
        )

        viewModel.load()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Failed to load", viewModel.state.error)
    }

    @Test
    fun `nextMonth increments month`() = runTest {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 1
        )

        viewModel.nextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2026, viewModel.state.year)
        assertEquals(2, viewModel.state.month)
    }

    @Test
    fun `nextMonth wraps year when December`() = runTest {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 12
        )

        viewModel.nextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2027, viewModel.state.year)
        assertEquals(1, viewModel.state.month)
    }

    @Test
    fun `prevMonth decrements month`() = runTest {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 3
        )

        viewModel.prevMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2026, viewModel.state.year)
        assertEquals(2, viewModel.state.month)
    }

    @Test
    fun `prevMonth wraps year when January`() = runTest {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 1
        )

        viewModel.prevMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2025, viewModel.state.year)
        assertEquals(12, viewModel.state.month)
    }
}

private class FakeCalendarRepository(
    private val returnCalendar: Calendar = Calendar(
        listOf(LocalDate(2026, 1, 1) to false)
    )
) : CalendarRepository(
    client = FakeGitHubClient(),
    settingRepository = FakeSettingRepository()
) {
    override suspend fun findByMonth(yearMonth: YearMonth): Calendar = returnCalendar
}

private class FakeGitHubClient : GitHubClient()

private class FakeSettingRepository : SettingRepository(
    settingFilePath = kotlin.io.path.createTempFile(),
    gitHubClient = FakeGitHubClient()
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> {
        return GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
    }
}
