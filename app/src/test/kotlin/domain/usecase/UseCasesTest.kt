package domain.usecase

import core.model.Result
import core.time.DateProvider
import data.auth.TokenValidator
import data.github.ContentsApi
import data.github.model.ContentFile
import data.repo.DiaryRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Base64

class UseCasesTest {

    private val fixedClock = Clock.fixed(Instant.parse("2024-01-15T12:00:00Z"), ZoneId.of("Asia/Tokyo"))
    private val dateProvider = DateProvider(fixedClock)

    @Test
    fun `fetch month diaries returns exists flags`() = runBlocking {
        val fakeApi = FakeContentsApi()
        // Jan 1 exists, Jan 2 missing
        fakeApi.setExists("2024/01/01/README.md", true)
        val diaryRepo = DiaryRepository(fakeApi)
        val useCase = FetchMonthDiariesUseCase(diaryRepo, dateProvider)

        val result = useCase("me", "repo", 2024, 1)

        val statuses = (result as Result.Success).value
        assertTrue(statuses.any { it.date.dayOfMonth == 1 && it.exists })
        assertTrue(statuses.any { it.date.dayOfMonth == 2 && !it.exists })
    }

    @Test
    fun `fetch diary returns decoded text`() = runBlocking {
        val fakeApi = FakeContentsApi()
        fakeApi.store("2024/01/10/README.md", "hello")
        val diaryRepo = DiaryRepository(fakeApi)
        val useCase = FetchDiaryUseCase(diaryRepo)

        val result = useCase("me", "repo", LocalDate.of(2024, 1, 10))

        assertEquals("hello", (result as Result.Success).value)
    }

    @Test
    fun `save diary delegates to repository`() = runBlocking {
        val fakeApi = FakeContentsApi()
        val diaryRepo = DiaryRepository(fakeApi)
        val useCase = SaveDiaryUseCase(diaryRepo)
        val date = LocalDate.of(2024, 1, 20)

        val result = useCase("me", "repo", date, "content")

        assertTrue(result is Result.Success)
        assertTrue(fakeApi.savedPaths.contains("2024/01/20/README.md"))
    }

    @Test
    fun `validate token delegates to validator`() = runBlocking {
        val validator = object : TokenValidator(HttpClient(MockEngine { respond("", HttpStatusCode.OK) })) {
            override suspend fun validate(token: String): Result<Boolean> = Result.Success(token == "ok")
        }
        val useCase = ValidateTokenUseCase(validator)

        val success = useCase("ok")
        val failure = useCase("ng")

        assertEquals(true, (success as Result.Success).value)
        assertEquals(false, (failure as Result.Success).value)
    }

    private class FakeContentsApi : ContentsApi(HttpClient(MockEngine { respond("", HttpStatusCode.OK) })) {
        val savedPaths = mutableSetOf<String>()
        private val exists = mutableSetOf<String>()
        private val contents = mutableMapOf<String, ContentFile>()

        fun setExists(path: String, value: Boolean) {
            if (value) {
                exists.add(path)
                if (!contents.containsKey(path)) {
                    val encoded = Base64.getEncoder().encodeToString("".toByteArray())
                    contents[path] = ContentFile(name = "README.md", path = path, sha = "sha", content = encoded, encoding = "base64")
                }
            } else {
                exists.remove(path)
                contents.remove(path)
            }
        }

        fun store(path: String, text: String) {
            val encoded = Base64.getEncoder().encodeToString(text.toByteArray())
            exists.add(path)
            contents[path] = ContentFile(name = "README.md", path = path, sha = "sha", content = encoded, encoding = "base64")
        }

        override suspend fun getContent(owner: String, repo: String, path: String, branch: String): Result<ContentFile?> {
            return if (exists.contains(path)) {
                Result.Success(contents[path])
            } else {
                Result.Success(null)
            }
        }

        override suspend fun putContent(owner: String, repo: String, path: String, rawContent: String, message: String, branch: String, sha: String?): Result<ContentFile> {
            savedPaths.add(path)
            store(path, rawContent)
            return Result.Success(contents[path]!!)
        }
    }
}