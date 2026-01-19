package io.github.t45k.githubDiary.setting

import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.GitHubClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Serialize/deserialize user settings for GitHub personal access token and repository path via a text file.
 */
open class SettingRepository(
    private val fileStorage: SettingFileStorage = createSettingFileStorage(),
    private val gitHubClient: GitHubClient,
) {
    private val json = Json { ignoreUnknownKeys = true }

    open suspend fun save(pat: GitHubPersonalAccessToken?, path: GitHubRepositoryPath?) {
        val encodedText = json.encodeToString(SettingFileJsonFormat(pat, path))
        fileStorage.write(encodedText)
    }

    open suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> {
        val encodedText = fileStorage.read()
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
