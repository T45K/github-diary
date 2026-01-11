package io.github.t45k.githubDiary.monthlyNote

import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubContentApiPathParam
import io.github.t45k.githubDiary.setting.SettingRepository
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

        val encodedContent = Base64.Default.encode(goal.content().toByteArray())

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
            String(Base64.Default.decode(contentWithNewLines.replace("\n", "").toByteArray()))
        }
        return if (decodedContent == null) GoalContent.init(yearMonth) else GoalContent.parse(decodedContent)
    }

    private fun buildPathParam(path: GitHubRepositoryPath, yearMonth: YearMonth): GitHubContentApiPathParam = GitHubContentApiPathParam(
        path.owner,
        path.name,
        yearMonth.format(githubPathFormat) + "/README.md",
    )

    private val githubPathFormat = YearMonth.Companion.Format {
        year()
        char('/')
        monthNumber()
    }
}
