package data.github

import core.model.Result
import data.github.model.ContentFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Base64

class ContentsApiTest {

    private val jsonConfig = Json { ignoreUnknownKeys = true }

    @Test
    fun `getContent returns file on 200`() = runBlocking {
        val encoded = Base64.getEncoder().encodeToString("hello".toByteArray())
        val body = jsonConfig.encodeToString(ContentFile(name = "README.md", path = "2024/01/01/README.md", sha = "abc", content = encoded, encoding = "base64"))
        val engine = mockEngine(HttpStatusCode.OK, body)
        val api = ContentsApi(buildClient(engine))

        val result = api.getContent("me", "repo", "2024/01/01/README.md")

        val file = (result as Result.Success).value
        assertEquals("README.md", file?.name)
        assertEquals("base64", file?.encoding)
    }

    @Test
    fun `getContent returns null on 404`() = runBlocking {
        val engine = mockEngine(HttpStatusCode.NotFound, "")
        val api = ContentsApi(buildClient(engine))

        val result = api.getContent("me", "repo", "missing.md")

        assertEquals(null, (result as Result.Success).value)
    }

    @Test
    fun `putContent sends base64 encoded body`() = runBlocking {
        var capturedBody: String? = null
        val responseBody = jsonConfig.encodeToString(mapOf("content" to mapOf("name" to "README.md", "path" to "2024/01/02/README.md", "sha" to "newsha")))
        val engine = MockEngine { request: HttpRequestData ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(responseBody, HttpStatusCode.Created, headers = headersOfJson())
        }
        val api = ContentsApi(buildClient(engine))

        val result = api.putContent("me", "repo", "2024/01/02/README.md", "hello world", "2024/01/02")

        val content = (result as Result.Success).value
        assertEquals("README.md", content.name)
        assertTrue(capturedBody!!.contains("aGVsbG8gd29ybGQ=")) // base64 of hello world
    }

    private fun headersOfJson() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun mockEngine(status: HttpStatusCode, body: String): MockEngine = MockEngine { request: HttpRequestData ->
        respond(body, status, headers = headersOfJson())
    }

    private fun buildClient(engine: MockEngine): HttpClient = HttpClient(engine) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }
}
