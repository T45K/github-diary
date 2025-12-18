package ui.calendar

import core.model.Result
import domain.usecase.FetchMonthDiariesUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CalendarViewModelTest {

    @Test
    fun `load populates days`() = runBlocking {
        val fakeUseCase = FakeFetchMonthUseCase()
        fakeUseCase.response = Result.Success(
            listOf(
                FetchMonthDiariesUseCase.DayStatus(LocalDate.of(2024, 1, 1), true),
                FetchMonthDiariesUseCase.DayStatus(LocalDate.of(2024, 1, 2), false)
            )
        )
        val vm = CalendarViewModel(fakeUseCase, 2024, 1)

        vm.load("me", "repo")

        assertEquals(2, vm.state.days.size)
        assertTrue(vm.state.days.first { it.date.dayOfMonth == 1 }.exists)
    }

    @Test
    fun `load handles failure`() = runBlocking {
        val fakeUseCase = FakeFetchMonthUseCase()
        fakeUseCase.response = Result.Failure("error")
        val vm = CalendarViewModel(fakeUseCase, 2024, 1)

        vm.load("me", "repo")

        assertEquals("error", vm.state.error)
    }

    private class FakeFetchMonthUseCase : FetchMonthDiariesUseCase(
        diaryRepository = data.repo.DiaryRepository(data.github.ContentsApi(io.ktor.client.HttpClient())),
        dateProvider = core.time.DateProvider()
    ) {
        var response: Result<List<FetchMonthDiariesUseCase.DayStatus>> = Result.Success(emptyList())
        override suspend operator fun invoke(owner: String, repo: String, year: Int?, month: Int?): Result<List<DayStatus>> {
            return response
        }
    }
}
