package core.repository

import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CalendarRepositoryTest {

    @Test
    fun `findByMonth returns empty calendar when no credentials`() = runTest {
        val settingRepo = CalendarRepoFakeSettingRepository(loadResult = null to null)
        val gitHubClient = CalendarRepoFakeGitHubClient()
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)

        val yearMonth = YearMonth(2026, 1)
        val result = calendarRepository.findByMonth(yearMonth)

        assertTrue(result.dates.isEmpty())
    }

    @Test
    fun `findByMonth returns calendar with all days when credentials exist`() = runTest {
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(contentExists = false)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)

        val yearMonth = YearMonth(2026, 1)
        val result = calendarRepository.findByMonth(yearMonth)

        assertEquals(31, result.dates.size)
        assertEquals(LocalDate(2026, 1, 1), result.dates.first().first)
        assertEquals(LocalDate(2026, 1, 31), result.dates.last().first)
    }

    @Test
    fun `findByMonth marks dates with content as true`() = runTest {
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
        val result = calendarRepository.findByMonth(yearMonth)

        val day5 = result.dates.find { it.first == LocalDate(2026, 1, 5) }
        val day15 = result.dates.find { it.first == LocalDate(2026, 1, 15) }
        val day1 = result.dates.find { it.first == LocalDate(2026, 1, 1) }

        assertTrue(day5!!.second)
        assertTrue(day15!!.second)
        assertFalse(day1!!.second)
    }

    @Test
    fun `findByMonth handles February correctly`() = runTest {
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(contentExists = false)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)

        val yearMonth = YearMonth(2026, 2)
        val result = calendarRepository.findByMonth(yearMonth)

        assertEquals(28, result.dates.size)
    }

    @Test
    fun `findByMonth handles leap year February correctly`() = runTest {
        val settingRepo = CalendarRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = CalendarRepoFakeGitHubClient(contentExists = false)
        val calendarRepository = CalendarRepository(gitHubClient, settingRepo)

        val yearMonth = YearMonth(2024, 2)
        val result = calendarRepository.findByMonth(yearMonth)

        assertEquals(29, result.dates.size)
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
    settingFilePath = kotlin.io.path.createTempFile(),
    gitHubClient = object : GitHubClient() {}
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult
}
