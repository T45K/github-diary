package io.github.t45k.githubDiary.diary

import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubContentApiPathParam
import io.github.t45k.githubDiary.setting.SettingRepository
import kotlin.io.encoding.Base64
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char

open class DiaryRepository(
    private val client: GitHubClient,
    private val settingRepository: SettingRepository,
) {
    open suspend fun save(diary: DiaryContent) {
        val (accessToken, repoPath) = settingRepository.load()
        if (accessToken == null || repoPath == null) {
            return
        }
        val existingBlobSha = client.getContent(
            accessToken,
            buildPathParam(repoPath, diary.date),
        )?.sha

        val encodedContent = Base64.Default.encode(diary.content.encodeToByteArray())

        client.putContent(
            accessToken,
            buildPathParam(repoPath, diary.date),
            diary.date.format(githubPathFormat),
            encodedContent,
            existingBlobSha,
        )
    }

    open suspend fun findByDate(date: LocalDate): DiaryContent {
        val (accessToken, repoPath) = settingRepository.load()
        if (accessToken == null || repoPath == null) {
            return DiaryContent.init(date)
        }

        val content = client.getContent(
            accessToken,
            buildPathParam(repoPath, date),
        )
        val decodedContent = content?.content?.let { contentWithNewLines ->
            Base64.Default.decode(contentWithNewLines.replace("\n", "")).decodeToString()
        }
        return if (decodedContent == null) DiaryContent.init(date) else DiaryContent(date, decodedContent)
    }

    private fun buildPathParam(path: GitHubRepositoryPath, date: LocalDate): GitHubContentApiPathParam = GitHubContentApiPathParam(
        path.owner,
        path.name,
        date.format(githubPathFormat) + "/README.md",
    )

    private val githubPathFormat = LocalDate.Companion.Format {
        year()
        char('/')
        monthNumber()
        char('/')
        day()
    }
}
