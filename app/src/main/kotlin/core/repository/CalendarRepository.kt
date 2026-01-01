package core.repository

import core.entity.Calendar
import core.entity.GitHubRepositoryPath
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

class CalendarRepository(
    private val client: GitHubClient,
    private val settingRepository: SettingRepository,
) {
    suspend fun findByMonth(yearMonth: YearMonth): Calendar {
        val (token, repoPath) = settingRepository.load()
        return if (token == null || repoPath == null) {
            Calendar(emptyList())
        } else {
            Calendar.init(yearMonth) {
                (client.getContent(token, buildPathParam(repoPath, date = it)) != null)
            }
        }
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
