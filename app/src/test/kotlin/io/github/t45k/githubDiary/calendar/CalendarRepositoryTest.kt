package io.github.t45k.githubDiary.calendar

import io.github.t45k.githubDiary.github.ContentFile
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.github.GitHubContentApiPathParam
import io.github.t45k.githubDiary.github.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.github.GitHubRepositoryPath
import io.github.t45k.githubDiary.setting.SettingRepository
import kotlin.io.path.createTempFile
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Test

class CalendarRepositoryTest {

    @Test
    fun `findByMonth returns empty calendar when no credentials`() = runTest {
        // given
        val settingRepo = CalendarRepoFakeSettingRepository(loadResult = null to null)
        val gitHubClient = CalendarRepoFakeGitHubClient()
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)
        val yearMonth = YearMonth(2026, 1)

        // when
        val result = calendarRepository.findByMonth(yearMonth)

        // then
        assert(result.days.isEmpty())
    }

    @Test
    fun `findByMonth returns calendar with all days when credentials exist`() = runTest {
        // given
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(contentExists = false)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)
        val yearMonth = YearMonth(2026, 1)

        // when
        val result = calendarRepository.findByMonth(yearMonth)

        // then
        assert(result.days.size == 31)
        assert(result.days.first().first == LocalDate(2026, 1, 1))
        assert(result.days.last().first == LocalDate(2026, 1, 31))
    }

    @Test
    fun `findByMonth marks dates with content as true`() = runTest {
        // given
        val datesWithContent = setOf(
            LocalDate(2026, 1, 5),
            LocalDate(2026, 1, 15)
        )
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(datesWithContent = datesWithContent)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)
        val yearMonth = YearMonth(2026, 1)

        // when
        val result = calendarRepository.findByMonth(yearMonth)

        // then
        val day5 = result.days.find { it.first == LocalDate(2026, 1, 5) }
        val day15 = result.days.find { it.first == LocalDate(2026, 1, 15) }
        val day1 = result.days.find { it.first == LocalDate(2026, 1, 1) }

        assert(day5!!.second == true)
        assert(day15!!.second == true)
        assert(day1!!.second == false)
    }

    @Test
    fun `findByMonth handles February correctly`() = runTest {
        // given
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(contentExists = false)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)
        val yearMonth = YearMonth(2026, 2)

        // when
        val result = calendarRepository.findByMonth(yearMonth)

        // then
        assert(result.days.size == 28)
    }

    @Test
    fun `findByMonth handles leap year February correctly`() = runTest {
        // given
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(contentExists = false)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)
        val yearMonth = YearMonth(2024, 2)

        // when
        val result = calendarRepository.findByMonth(yearMonth)

        // then
        assert(result.days.size == 29)
    }
}

private class CalendarRepoFakeGitHubClient(
    private val contentExists: Boolean = false,
    private val datesWithContent: Set<LocalDate> = emptySet()
) : GitHubClient() {
    override suspend fun getContent(
        accessToken: GitHubPersonalAccessToken,
        pathParam: GitHubContentApiPathParam
    ): ContentFile? {
        val pathParts = pathParam.path.split("/")
        if (pathParts.size >= 3) {
            val year = pathParts[0].toIntOrNull()
            val month = pathParts[1].toIntOrNull()
            val day = pathParts[2].toIntOrNull()
            if (year != null && month != null && day != null) {
                val date = LocalDate(year, month, day)
                if (date in datesWithContent) {
                    return ContentFile("sha", "content")
                }
            }
        }
        return if (contentExists) ContentFile("sha", "content") else null
    }
}

private class CalendarRepoFakeSettingRepository(
    private val loadResult: Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = null to null
) : SettingRepository(
    settingFilePath = createTempFile(),
    gitHubClient = object : GitHubClient() {}
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult
}
