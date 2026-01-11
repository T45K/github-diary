package io.github.t45k.githubDiary.calendar

import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubContentApiPathParam
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.setting.SettingRepository
import io.github.t45k.githubDiary.util.localDateSlashFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format

open class CalendarRepository(
    private val client: GitHubClient,
    private val settingRepository: SettingRepository,
) {
    open suspend fun findByMonth(yearMonth: YearMonth): Calendar {
        val (token, repoPath) = settingRepository.load()
        return if (token == null || repoPath == null) {
            Calendar(yearMonth, emptyList())
        } else {
            Calendar.init(yearMonth) {
                (client.getContent(token, buildPathParam(repoPath, date = it)) != null)
            }
        }
    }

    private fun buildPathParam(path: GitHubRepositoryPath, date: LocalDate): GitHubContentApiPathParam = GitHubContentApiPathParam(
        path.owner,
        path.name,
        date.format(localDateSlashFormat) + "/README.md",
    )
}
