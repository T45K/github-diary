package io.github.t45k.githubDiary.setting

import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.GitHubClient
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE

/**
 * Serialize/deserialize user settings for GitHub personal access token and repository path via a text file.
 */
open class SettingRepository(
    private val settingFilePath: Path = Path(System.getenv("HOME"), ".github_diary", "settings.json"),
    private val gitHubClient: GitHubClient,
) {
    init {
        settingFilePath.createParentDirectories()
        if (settingFilePath.notExists()) {
            settingFilePath.createFile()
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    open suspend fun save(pat: GitHubPersonalAccessToken?, path: GitHubRepositoryPath?) {
        val encodedText = json.encodeToString(SettingFileJsonFormat(pat, path))
        withContext(Dispatchers.IO) { settingFilePath.writeText(encodedText, options = arrayOf(WRITE, TRUNCATE_EXISTING)) }
    }

    open suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> {
        val encodedText = withContext(Dispatchers.IO) { settingFilePath.readText() }
        val json = try {
            json.decodeFromString<SettingFileJsonFormat>(encodedText)
        } catch (_: Exception) {
            save(null, null)
            return null to null
        }
        val pat = json.pat?.let { GitHubPersonalAccessToken(it) }
        val path = json.path?.let { GitHubRepositoryPath(it).getOrNull()!! }
        return pat to path
    }

    open suspend fun hasPermission(pat: GitHubPersonalAccessToken, path: GitHubRepositoryPath): Boolean {
        return gitHubClient.getRepository(pat, path.owner, path.name) != null
    }
}

@Serializable
private data class SettingFileJsonFormat(
    val pat: String?,
    val path: String?,
) {
    constructor(
        pat: GitHubPersonalAccessToken?,
        path: GitHubRepositoryPath?,
    ) : this(
        pat = pat?.value,
        path = path?.toString(),
    )

}
