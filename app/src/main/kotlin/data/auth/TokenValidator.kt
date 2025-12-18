package data.auth

import core.AppConfig
import core.model.Result
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

open class TokenValidator(
    private val client: HttpClient
) {
    open suspend fun validate(token: String): Result<Boolean> {
        return runCatching {
            client.get("${AppConfig.githubApiBaseUrl}/user") {
                header("Authorization", "token $token")
            }
        }.fold(
            onSuccess = { response -> handleResponse(response) },
            onFailure = { throwable -> Result.Failure("Failed to validate token", throwable) }
        )
    }

    private suspend fun handleResponse(response: HttpResponse): Result<Boolean> {
        return when (response.status) {
            HttpStatusCode.OK -> Result.Success(true)
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> Result.Success(false)
            else -> Result.Failure("Unexpected status: ${response.status}")
        }
    }
}
