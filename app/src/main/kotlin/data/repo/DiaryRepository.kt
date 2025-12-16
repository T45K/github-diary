package data.repo

import core.model.Result
import core.time.DateFormatter
import data.github.ContentsApi
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.util.Base64

open class DiaryRepository(
    private val contentsApi: ContentsApi
) {
    open suspend fun getDiary(owner: String, repo: String, date: LocalDate): Result<String?> {
        val path = DateFormatter.buildPath(date)
        return when (val result = contentsApi.getContent(owner, repo, path)) {
            is Result.Success -> {
                val file = result.value ?: return Result.Success(null)
                val decoded = decodeBase64(file.content)
                Result.Success(decoded)
            }
            is Result.Failure -> result
        }
    }

    open suspend fun saveDiary(
        owner: String,
        repo: String,
        date: LocalDate,
        content: String,
        sha: String? = null
    ): Result<Unit> {
        val path = DateFormatter.buildPath(date)
        val message = date.toString()
        return when (val result = contentsApi.putContent(owner, repo, path, content, message, sha = sha)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Failure -> result
        }
    }

    open suspend fun exists(owner: String, repo: String, date: LocalDate): Result<Boolean> {
        val path = DateFormatter.buildPath(date)
        return when (val result = contentsApi.getContent(owner, repo, path)) {
            is Result.Success -> Result.Success(result.value != null)
            is Result.Failure -> result
        }
    }

    private fun decodeBase64(content: String?): String? {
        if (content == null) return null
        return try {
            val bytes = Base64.getDecoder().decode(content)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}