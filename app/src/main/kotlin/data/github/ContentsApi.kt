package data.github

import core.AppConfig
import core.model.Result
import data.github.model.ContentFile
import data.github.model.PutContentRequest
import data.github.model.PutContentResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.util.Base64

class ContentsApi(
    private val client: HttpClient
) {
    suspend fun getContent(owner: String, repo: String, path: String, branch: String = AppConfig.defaultBranch): Result<ContentFile?> {
        val url = "${AppConfig.githubApiBaseUrl}/repos/$owner/$repo/contents/$path"
        return runCatching {
            client.get(url) {
                parameter("ref", branch)
            }
        }.fold(
            onSuccess = { response ->
                when (response.status) {
                    HttpStatusCode.OK -> Result.Success(response.body())
                    HttpStatusCode.NotFound -> Result.Success(null)
                    else -> Result.Failure("Unexpected status: ${response.status}")
                }
            },
            onFailure = { throwable -> Result.Failure("Failed to fetch content", throwable) }
        )
    }

    suspend fun putContent(
        owner: String,
        repo: String,
        path: String,
        rawContent: String,
        message: String,
        branch: String = AppConfig.defaultBranch,
        sha: String? = null
    ): Result<ContentFile> {
        val url = "${AppConfig.githubApiBaseUrl}/repos/$owner/$repo/contents/$path"
        val encoded = Base64.getEncoder().encodeToString(rawContent.toByteArray(Charsets.UTF_8))
        val requestBody = PutContentRequest(
            message = message,
            content = encoded,
            branch = branch,
            sha = sha
        )

        return runCatching {
            client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
        }.fold(
            onSuccess = { response ->
                when (response.status) {
                    HttpStatusCode.Created, HttpStatusCode.OK -> Result.Success(response.body<PutContentResponse>().content)
                    HttpStatusCode.Conflict -> Result.Failure("Conflict: ${response.status}")
                    else -> Result.Failure("Unexpected status: ${response.status}")
                }
            },
            onFailure = { throwable -> Result.Failure("Failed to put content", throwable) }
        )
    }
}