package data.auth

import core.model.Result
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class TokenStore(
    private val filePath: Path = Paths.get(System.getProperty("user.home"), ".github_diary", "token.json"),
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true }
) {

    @Serializable
    data class StoredToken(
        val token: String,
        val mode: AuthMode
    )

    fun save(token: StoredToken): Result<Unit> {
        return runCatching {
            Files.createDirectories(filePath.parent)
            Files.writeString(filePath, json.encodeToString(token))
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure("Failed to save token", it) }
        )
    }

    fun load(): Result<StoredToken?> {
        if (!Files.exists(filePath)) {
            return Result.Success(null)
        }
        return runCatching {
            val content = Files.readString(filePath)
            json.decodeFromString<StoredToken>(content)
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Failure("Failed to load token", it) }
        )
    }

    fun clear(): Result<Unit> {
        return runCatching {
            Files.deleteIfExists(filePath)
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure("Failed to clear token", it) }
        )
    }
}