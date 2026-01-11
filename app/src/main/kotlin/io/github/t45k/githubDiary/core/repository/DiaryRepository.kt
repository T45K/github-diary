package io.github.t45k.githubDiary.core.repository

import io.github.t45k.githubDiary.core.entity.DiaryContent
import io.github.t45k.githubDiary.core.entity.GitHubRepositoryPath
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

        val encodedContent = Base64.encode(diary.content.toByteArray())

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
            String(Base64.decode(contentWithNewLines.replace("\n", "").toByteArray()))
        }
        return if (decodedContent == null) DiaryContent.init(date) else DiaryContent(date, decodedContent)
    }

    private fun buildPathParam(path: GitHubRepositoryPath, date: LocalDate): GitHubContentApiPathParam = GitHubContentApiPathParam(
        path.owner,
        path.name,
        date.format(githubPathFormat) + "/README.md",
    )

    private val githubPathFormat = LocalDate.Format {
        year()
        char('/')
        monthNumber()
        char('/')
        day()
    }
}
