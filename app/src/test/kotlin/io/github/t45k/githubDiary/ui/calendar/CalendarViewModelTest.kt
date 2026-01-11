package io.github.t45k.githubDiary.ui.calendar

import io.github.t45k.githubDiary.core.entity.Calendar
import io.github.t45k.githubDiary.core.entity.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.core.entity.GitHubRepositoryPath
import io.github.t45k.githubDiary.core.repository.CalendarRepository
import io.github.t45k.githubDiary.core.repository.GitHubClient
import io.github.t45k.githubDiary.core.repository.SettingRepository
import kotlin.io.path.createTempFile
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
            initialYear = 2026,
            initialMonth = 1
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

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Success)
        val successState = state as CalendarUiState.Success
        assert(successState.year == 2026)
        assert(successState.month == 1)
        assert(successState.days.size == 2)
        assert(successState.days[0].exists == true)
        assert(successState.days[1].exists == false)
    }

    @Test
    fun `state becomes Error when calendar is empty`() = runTest {
        // given
        val fakeRepo = FakeCalendarRepository(returnCalendar = Calendar(emptyList()))
        val viewModel = CalendarViewModel(
            calendarRepository = fakeRepo,
            initialYear = 2026,
            initialMonth = 1
        )

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Error)
        val errorState = state as CalendarUiState.Error
        assert(errorState.year == 2026)
        assert(errorState.month == 1)
        assert(errorState.message == "Failed to load")
    }

    @Test
    fun `Success state has correct year and month`() = runTest {
        // given
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

        // when
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value

        // then
        assert(state is CalendarUiState.Success)
        val successState = state as CalendarUiState.Success
        assert(successState.year == 2026)
        assert(successState.month == 6)
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
    settingFilePath = createTempFile(),
    gitHubClient = FakeGitHubClient()
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> {
        return GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
    }
}
