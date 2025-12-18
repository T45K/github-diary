package data.github

import core.AppConfig
import core.model.Result
import data.github.model.ContentFile
import data.github.model.PutContentRequest
import data.github.model.PutContentResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.util.Base64

open class ContentsApi(
    private val client: HttpClient
) {
    open suspend fun getContent(owner: String, repo: String, path: String, branch: String = AppConfig.defaultBranch): Result<ContentFile?> {
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
                    else -> {
                        println("[DEBUG_LOG] GET contents unexpected status=${response.status} url=$url")
                        Result.Failure("Unexpected status: ${response.status}")
                    }
                }
            },
            onFailure = { throwable ->
                println("[DEBUG_LOG] GET contents failed url=$url error=${throwable.message}")
                Result.Failure("Failed to fetch content", throwable)
            }
        )
    }

    open suspend fun putContent(
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
                    HttpStatusCode.Conflict -> {
                        println("[DEBUG_LOG] PUT contents conflict url=$url")
                        Result.Failure("Conflict: ${response.status}")
                    }

                    else -> {
                        println("[DEBUG_LOG] PUT contents unexpected status=${response.status} url=$url")
                        Result.Failure("Unexpected status: ${response.status}")
                    }
                }
            },
            onFailure = { throwable ->
                println("[DEBUG_LOG] PUT contents failed url=$url error=${throwable.message}")
                Result.Failure("Failed to put content", throwable)
            }
        )
    }
}
