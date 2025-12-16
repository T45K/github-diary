package ui.preview

import core.model.Result
import domain.usecase.FetchDiaryUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PreviewViewModelTest {

    @Test
    fun `sets notFound when diary missing`() = runBlocking {
        val vm = PreviewViewModel(FakeFetchDiary(Result.Success(null)), "me", "repo", LocalDate.of(2024, 1, 1))

        vm.load()

        assertTrue(vm.state.notFound)
    }

    @Test
    fun `loads content when exists`() = runBlocking {
        val vm = PreviewViewModel(FakeFetchDiary(Result.Success("hello")), "me", "repo", LocalDate.of(2024, 1, 2))

        vm.load()

        assertEquals("hello", vm.state.content)
    }

    @Test
    fun `sets error on failure`() = runBlocking {
        val vm = PreviewViewModel(FakeFetchDiary(Result.Failure("err")), "me", "repo", LocalDate.of(2024, 1, 3))

        vm.load()

        assertEquals("err", vm.state.error)
    }

    private class FakeFetchDiary(private val result: Result<String?>) : FetchDiaryUseCase(
        diaryRepository = data.repo.DiaryRepository(data.github.ContentsApi(io.ktor.client.HttpClient()))
    ) {
        override suspend fun invoke(owner: String, repo: String, date: LocalDate): Result<String?> = result
    }
}