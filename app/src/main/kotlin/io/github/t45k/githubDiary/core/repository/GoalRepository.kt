package io.github.t45k.githubDiary.core.repository

import io.github.t45k.githubDiary.core.entity.GitHubRepositoryPath
import io.github.t45k.githubDiary.core.entity.GoalContent
import kotlin.io.encoding.Base64
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

open class GoalRepository(
    private val client: GitHubClient,
    private val settingRepository: SettingRepository,
) {
    open suspend fun save(goal: GoalContent) {
        val (accessToken, repoPath) = settingRepository.load()
        if (accessToken == null || repoPath == null) {
            return
        }
        val existingBlobSha = client.getContent(
            accessToken,
            buildPathParam(repoPath, goal.yearMonth),
        )?.sha

        val encodedContent = Base64.encode(goal.content().toByteArray())

        client.putContent(
            accessToken,
            buildPathParam(repoPath, goal.yearMonth),
            goal.yearMonth.format(githubPathFormat),
            encodedContent,
            existingBlobSha,
        )
    }

    open suspend fun findByYearMonth(yearMonth: YearMonth): GoalContent {
        val (accessToken, repoPath) = settingRepository.load()
        if (accessToken == null || repoPath == null) {
            return GoalContent.init(yearMonth)
        }

        val content = client.getContent(
            accessToken,
            buildPathParam(repoPath, yearMonth),
        )
        val decodedContent = content?.content?.let { contentWithNewLines ->
            String(Base64.decode(contentWithNewLines.replace("\n", "").toByteArray()))
        }
        return if (decodedContent == null) GoalContent.init(yearMonth) else GoalContent.parse(decodedContent)
    }

    private fun buildPathParam(path: GitHubRepositoryPath, yearMonth: YearMonth): GitHubContentApiPathParam = GitHubContentApiPathParam(
        path.owner,
        path.name,
        yearMonth.format(githubPathFormat) + "/README.md",
    )

    private val githubPathFormat = YearMonth.Format {
        year()
        char('/')
        monthNumber()
    }
}
