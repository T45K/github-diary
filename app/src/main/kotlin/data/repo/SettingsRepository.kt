package data.repo

import core.model.Result
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SettingsRepository(
    private val filePath: Path = Paths.get(System.getProperty("user.home"), ".github_diary", "repo.txt")
) {
    fun save(ownerRepo: String): Result<Unit> {
        if (!isValid(ownerRepo)) {
            return Result.Failure("Invalid repo format. Use org/repo")
        }
        return runCatching {
            Files.createDirectories(filePath.parent)
            Files.writeString(filePath, ownerRepo.trim())
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure("Failed to save repo", it) }
        )
    }

    fun load(): Result<String?> {
        if (!Files.exists(filePath)) return Result.Success(null)
        return runCatching { Files.readString(filePath).trim() }.fold(
            onSuccess = { Result.Success(if (it.isBlank()) null else it) },
            onFailure = { Result.Failure("Failed to load repo", it) }
        )
    }

    fun clear(): Result<Unit> {
        return runCatching { Files.deleteIfExists(filePath) }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure("Failed to clear repo", it) }
        )
    }

    fun isValid(ownerRepo: String): Boolean {
        return OWNER_REPO_REGEX.matches(ownerRepo.trim())
    }

    companion object {
        private val OWNER_REPO_REGEX = "^[^/]+/[^/]+$".toRegex()
    }
}