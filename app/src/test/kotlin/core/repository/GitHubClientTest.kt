package core.repository

import core.entity.GitHubPersonalAccessToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class GitHubClientTest {

    @Test
    fun `getContent returns ContentFile on success`() = runTest {
        // given
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"sha": "abc123", "content": "SGVsbG8gV29ybGQ="}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val gitHubClient = GitHubClientTestable(client)
        val token = GitHubPersonalAccessToken("test-token")
        val pathParam = GitHubContentApiPathParam("owner", "repo", "path/to/file.md")

        // when
        val result = gitHubClient.getContent(token, pathParam)

        // then
        assert(result != null)
        assert(result?.sha == "abc123")
        assert(result?.content == "SGVsbG8gV29ybGQ=")
    }

    @Test
    fun `getContent returns null on NotFound`() = runTest {
        // given
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"message": "Not Found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val gitHubClient = GitHubClientTestable(client)
        val token = GitHubPersonalAccessToken("test-token")
        val pathParam = GitHubContentApiPathParam("owner", "repo", "nonexistent.md")

        // when
        val result = gitHubClient.getContent(token, pathParam)

        // then
        assert(result == null)
    }

    @Test
    fun `getRepository returns response on success`() = runTest {
        // given
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"id": 12345}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val gitHubClient = GitHubClientTestable(client)
        val token = GitHubPersonalAccessToken("test-token")

        // when
        val result = gitHubClient.getRepository(token, "owner", "repo")

        // then
        assert(result != null)
        assert(result?.id == 12345L)
    }

    @Test
    fun `getRepository returns null on failure`() = runTest {
        // given
        val mockEngine = MockEngine { _ ->
            respond(
                content = """{"message": "Not Found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val gitHubClient = GitHubClientTestable(client)
        val token = GitHubPersonalAccessToken("test-token")

        // when
        val result = gitHubClient.getRepository(token, "owner", "nonexistent")

        // then
        assert(result == null)
    }
}

private class GitHubClientTestable(private val testClient: HttpClient) {
    suspend fun getContent(accessToken: GitHubPersonalAccessToken, pathParam: GitHubContentApiPathParam): ContentFile? {
        val response = testClient.get {
            url.path("repos", pathParam.owner, pathParam.repo, "contents", pathParam.path)
            bearerAuth(accessToken.value)
        }
        return when (response.status) {
            HttpStatusCode.OK -> response.body<ContentFile>()
            HttpStatusCode.NotFound -> null
            else -> throw RuntimeException("Failed")
        }
    }

    suspend fun getRepository(
        accessToken: GitHubPersonalAccessToken,
        owner: String,
        repo: String,
    ): GetRepositoryResponse? {
        val response = testClient.get {
            url.path("repos", owner, repo)
            bearerAuth(accessToken.value)
        }
        return if (response.status.isSuccess()) {
            response.body<GetRepositoryResponse>()
        } else {
            null
        }
    }
}
