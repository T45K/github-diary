package ui.edit

import core.model.Result
import data.repo.DiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EditViewModelTest {

    @Test
    fun `initial content has header`() {
        val vm = EditViewModel(FakeDiaryRepo(), "me", "repo", LocalDate.of(2024, 1, 1), CoroutineScope(Dispatchers.Unconfined))

        assertTrue(vm.state.content.startsWith("# 2024/01/01"))
    }

    @Test
    fun `load existing fills content`() = runBlocking {
        val repo = FakeDiaryRepo()
        repo.existing = "hello"
        val vm = EditViewModel(repo, "me", "repo", LocalDate.of(2024, 1, 2), CoroutineScope(Dispatchers.Unconfined))

        vm.loadExisting()

        assertEquals("hello", vm.state.content)
    }

    @Test
    fun `save sets error on failure`() = runBlocking {
        val repo = FakeDiaryRepo(saveResult = Result.Failure("fail"))
        val vm = EditViewModel(repo, "me", "repo", LocalDate.of(2024, 1, 3), CoroutineScope(Dispatchers.Unconfined))

        vm.save()

        assertEquals("fail", vm.state.error)
    }

    private class FakeDiaryRepo(
        private val saveResult: Result<Unit> = Result.Success(Unit)
    ) : DiaryRepository(data.github.ContentsApi(io.ktor.client.HttpClient())) {
        var existing: String? = null
        override suspend fun getDiary(owner: String, repo: String, date: LocalDate): Result<String?> {
            return Result.Success(existing)
        }

        override suspend fun saveDiary(owner: String, repo: String, date: LocalDate, content: String, sha: String?): Result<Unit> {
            return saveResult
        }
    }
}