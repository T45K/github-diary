package data.github

import core.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object GitHubClient {
    fun create(
        tokenProvider: () -> String?,
        engine: HttpClientEngine? = null,
        json: Json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    ): HttpClient {
        val actualEngine = engine ?: CIO.create()
        return HttpClient(actualEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        // avoid leaking tokens in logs
                        println("[HTTP] $message")
                    }
                }
            }
            defaultRequest {
                url(AppConfig.githubApiBaseUrl)
                tokenProvider()?.let { header("Authorization", "token $it") }
                header("Accept", "application/vnd.github+json")
            }
        }
    }
}
