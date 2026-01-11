package io.github.t45k.githubDiary.core.repository

import io.github.t45k.githubDiary.core.entity.GitHubPersonalAccessToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * https://docs.github.com/ja/rest/repos/contents
 */
open class GitHubClient(private val host: String = "https://api.github.com") {
    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                },
            )
        }
        defaultRequest {
            url(this@GitHubClient.host)
            accept(ContentType.parse("application/vnd.github+json"))
        }
    }

    open suspend fun getContent(accessToken: GitHubPersonalAccessToken, pathParam: GitHubContentApiPathParam): ContentFile? {
        val response = client.get {
            url.path("repos", pathParam.owner, pathParam.repo, "contents", pathParam.path)
            bearerAuth(accessToken.value)
        }
        return when (response.status) {
            HttpStatusCode.OK -> response.body()
            HttpStatusCode.NotFound -> null

            else -> {
                throw RuntimeException("Failed GET content: $pathParam, response: ${response.body<String>()}")
            }
        }
    }

    /**
     * @param content is encoded in Base64
     * @param sha is necessary when updating an existing content
     */
    open suspend fun putContent(
        accessToken: GitHubPersonalAccessToken,
        pathParam: GitHubContentApiPathParam,
        message: String,
        content: String,
        sha: String? = null,
    ) {
        val response = client.put {
            url.path("repos", pathParam.owner, pathParam.repo, "contents", pathParam.path)
            bearerAuth(accessToken.value)
            contentType(ContentType.Application.Json)
            setBody(
                PutContentRequest(
                    message,
                    content,
                    sha,
                ),
            )
        }

        if (!response.status.isSuccess()) {
            throw RuntimeException("Failed PUT content: $pathParam, message: $message, sha: $sha, response: ${response.body<String>()}")
        }
    }

    open suspend fun getRepository(
        accessToken: GitHubPersonalAccessToken,
        owner: String,
        repo: String,
    ): GetRepositoryResponse? {
        val response = client.get {
            url.path("repos", owner, repo)
            bearerAuth(accessToken.value)
        }
        return if (response.status.isSuccess()) {
            response.body()
        } else {
            null
        }
    }
}

data class GitHubContentApiPathParam(
    val owner: String,
    val repo: String,
    val path: String,
)

/**
 * @param content is encoded in Base64
 */
@Serializable
data class ContentFile(
    val sha: String,
    val content: String,
)

/**
 * @param content is encoded in Base64
 */
@Serializable
private data class PutContentRequest(
    val message: String,
    val content: String,
    val sha: String? = null,
)

@Serializable
data class GetRepositoryResponse(val id: Long)
