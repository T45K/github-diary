package data.repo

import core.model.Result
import data.github.ContentsApi
import data.github.model.ContentFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Base64

class DiaryRepositoryTest {

    @Test
    fun `returns decoded content when exists`() = runBlocking {
        val fakeApi = FakeContentsApi()
        val date = LocalDate.of(2024, 1, 1)
        fakeApi.store("2024/01/01/README.md", "hello")
        val repo = DiaryRepository(fakeApi)

        val result = repo.getDiary("me", "repo", date)

        assertEquals("hello", (result as Result.Success).value)
    }

    @Test
    fun `returns null when not exists`() = runBlocking {
        val repo = DiaryRepository(FakeContentsApi())

        val result = repo.getDiary("me", "repo", LocalDate.of(2024, 1, 2))

        assertEquals(null, (result as Result.Success).value)
    }

    @Test
    fun `save delegates to api`() = runBlocking {
        val fakeApi = FakeContentsApi()
        val repo = DiaryRepository(fakeApi)
        val date = LocalDate.of(2024, 1, 3)

        val result = repo.saveDiary("me", "repo", date, "content")

        assertTrue(result is Result.Success)
        assertTrue(fakeApi.savedPaths.contains("2024/01/03/README.md"))
    }

    private class FakeContentsApi : ContentsApi(HttpClient(MockEngine { respond("", HttpStatusCode.OK) })) {
        val savedPaths = mutableSetOf<String>()
        private val contents = mutableMapOf<String, ContentFile>()

        fun store(path: String, text: String) {
            val encoded = Base64.getEncoder().encodeToString(text.toByteArray())
            contents[path] = ContentFile(name = "README.md", path = path, sha = "sha", content = encoded, encoding = "base64")
        }

        override suspend fun getContent(owner: String, repo: String, path: String, branch: String): Result<ContentFile?> {
            return Result.Success(contents[path])
        }

        override suspend fun putContent(owner: String, repo: String, path: String, rawContent: String, message: String, branch: String, sha: String?): Result<ContentFile> {
            savedPaths.add(path)
            val encoded = Base64.getEncoder().encodeToString(rawContent.toByteArray())
            val file = ContentFile(name = "README.md", path = path, sha = sha ?: "new", content = encoded, encoding = "base64")
            contents[path] = file
            return Result.Success(file)
        }
    }
}