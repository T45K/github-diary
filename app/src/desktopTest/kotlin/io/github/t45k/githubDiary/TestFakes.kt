package io.github.t45k.githubDiary

import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.setting.SettingFileStorage
import io.github.t45k.githubDiary.setting.SettingRepository

open class FakeSettingRepository(
    private val loadResult: Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = null to null,
) : SettingRepository(
    fileStorage = object : SettingFileStorage {
        override suspend fun read(): String = ""
        override suspend fun write(content: String) {}
    },
    gitHubClient = object : GitHubClient() {},
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult
}
