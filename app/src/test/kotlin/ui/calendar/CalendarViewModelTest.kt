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
    fun `initial state is Loading`() {
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            initialYear = 2026,
            initialMonth = 1
        )

        assertTrue(viewModel.uiState.value is CalendarUiState.Loading)
    }

    @Test
    fun `after loading, state becomes Success with days`() = runTest {
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

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CalendarUiState.Success)
        val successState = state as CalendarUiState.Success
        assertEquals(2026, successState.year)
        assertEquals(1, successState.month)
        assertEquals(2, successState.days.size)
        assertTrue(successState.days[0].exists)
        assertFalse(successState.days[1].exists)
    }

    @Test
    fun `state becomes Error when calendar is empty`() = runTest {
        val fakeRepo = FakeCalendarRepository(returnCalendar = Calendar(emptyList()))
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            initialYear = 2026,
            initialMonth = 1
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CalendarUiState.Error)
        val errorState = state as CalendarUiState.Error
        assertEquals(2026, errorState.year)
        assertEquals(1, errorState.month)
        assertEquals("Failed to load", errorState.message)
    }

    @Test
    fun `Success state has correct year and month`() = runTest {
        val fakeRepo = FakeCalendarRepository(
            returnCalendar = Calendar(
                listOf(LocalDate(2026, 6, 15) to true)
            )
        )
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            initialYear = 2026,
            initialMonth = 6
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CalendarUiState.Success)
        val successState = state as CalendarUiState.Success
        assertEquals(2026, successState.year)
        assertEquals(6, successState.month)
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
