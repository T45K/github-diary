package io.github.t45k.githubDiary.calendar

import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.setting.SettingFileStorage
import io.github.t45k.githubDiary.setting.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.AfterEach
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
        // given
        val viewModel = CalendarViewModel(
            calendarRepository = FakeCalendarRepository(),
            calendarRefreshEvent = CalendarRefreshEvent(),
            yearMonth = YearMonth(2026, 1),
        )

        // when
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Loading)
    }

    @Test
    fun `after loading, state becomes Success with days`() = runTest {
        // given
        val fakeRepo = FakeCalendarRepository(
            returnCalendar = Calendar(
                yearMonth = YearMonth(2026, 1),
                days = listOf(
                    LocalDate(2026, 1, 1) to true,
                    LocalDate(2026, 1, 2) to false,
                ),
            ),
        )
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            calendarRefreshEvent = CalendarRefreshEvent(),
            yearMonth = YearMonth(2026, 1),
        )

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Success)
        val successState = state as CalendarUiState.Success
        assert(successState.yearMonth == YearMonth(2026, 1))
        assert(successState.calendar.days.size == 2)
        assert(successState.calendar.days[0].second == true)
        assert(successState.calendar.days[1].second == false)
    }

    @Test
    fun `state becomes Error when calendar is empty`() = runTest {
        // given
        val fakeRepo = FakeCalendarRepository(
            returnCalendar = Calendar(
                yearMonth = YearMonth(2026, 1),
                days = emptyList(),
            ),
        )
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            calendarRefreshEvent = CalendarRefreshEvent(),
            yearMonth = YearMonth(2026, 1),
        )

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Error)
        val errorState = state as CalendarUiState.Error
        assert(errorState.yearMonth == YearMonth(2026, 1))
        assert(errorState.message == "Failed to load")
    }

    @Test
    fun `Success state has correct yearMonth`() = runTest {
        // given
        val fakeRepo = FakeCalendarRepository(
            returnCalendar = Calendar(
                yearMonth = YearMonth(2026, 6),
                days = listOf(LocalDate(2026, 6, 15) to true),
            ),
        )
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            calendarRefreshEvent = CalendarRefreshEvent(),
            yearMonth = YearMonth(2026, 6),
        )

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Success)
        val successState = state as CalendarUiState.Success
        assert(successState.yearMonth == YearMonth(2026, 6))
    }
}

private class FakeCalendarRepository(
    private val returnCalendar: Calendar = Calendar(
        yearMonth = YearMonth(2026, 1),
        days = listOf(LocalDate(2026, 1, 1) to false),
    ),
) : CalendarRepository(
    client = FakeGitHubClient(),
    settingRepository = FakeSettingRepository(),
) {
    override suspend fun findByMonth(yearMonth: YearMonth): Calendar = returnCalendar
}

private class FakeGitHubClient : GitHubClient()

private class FakeSettingRepository : SettingRepository(
    fileStorage = object : SettingFileStorage {
        override suspend fun read(): String = ""
        override suspend fun write(content: String) {}
    },
    gitHubClient = FakeGitHubClient(),
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> {
        return GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner/repo").getOrNull()
    }
}
