package core.repository

import core.entity.DiaryContent
import core.entity.GitHubPersonalAccessToken
import core.entity.GitHubRepositoryPath
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.io.encoding.Base64

class DiaryRepositoryTest {

    @Test
    fun `findByDate returns default DiaryContent when no credentials`() = runTest {
        val settingRepo = DiaryRepoFakeSettingRepository(loadResult = null to null)
        val gitHubClient = DiaryRepoFakeGitHubClient()
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)

        val date = LocalDate(2026, 1, 2)
        val result = diaryRepository.findByDate(date)

        assertEquals(date, result.date)
        assertEquals(DiaryContent.init(date).content, result.content)
    }

    @Test
    fun `findByDate returns default DiaryContent when content not found`() = runTest {
        val settingRepo = DiaryRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = DiaryRepoFakeGitHubClient(contentResponse = null)
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)

        val date = LocalDate(2026, 1, 2)
        val result = diaryRepository.findByDate(date)

        assertEquals(date, result.date)
        assertEquals(DiaryContent.init(date).content, result.content)
    }

    @Test
    fun `findByDate returns DiaryContent from repository`() = runTest {
        val expectedContent = "# 2026/01/02 (Fri)\n\nDiary entry"
        val encodedContent = Base64.encode(expectedContent.toByteArray())
        val settingRepo = DiaryRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = DiaryRepoFakeGitHubClient(contentResponse = ContentFile("sha123", encodedContent))
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)

        val date = LocalDate(2026, 1, 2)
        val result = diaryRepository.findByDate(date)

        assertEquals(date, result.date)
        assertEquals(expectedContent, result.content)
    }

    @Test
    fun `save does nothing when no credentials`() = runTest {
        val settingRepo = DiaryRepoFakeSettingRepository(loadResult = null to null)
        val gitHubClient = DiaryRepoFakeGitHubClient()
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)

        val diary = DiaryContent(LocalDate(2026, 1, 2), "content")
        diaryRepository.save(diary)

        assertEquals(0, gitHubClient.putContentCallCount)
    }

    @Test
    fun `save calls putContent when credentials exist`() = runTest {
        val settingRepo = DiaryRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = DiaryRepoFakeGitHubClient()
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)

        val diary = DiaryContent(LocalDate(2026, 1, 2), "content")
        diaryRepository.save(diary)

        assertEquals(1, gitHubClient.putContentCallCount)
        assertNotNull(gitHubClient.lastPutContentPathParam)
        assertEquals("owner", gitHubClient.lastPutContentPathParam?.owner)
        assertEquals("repo", gitHubClient.lastPutContentPathParam?.repo)
        assertEquals("2026/01/02/README.md", gitHubClient.lastPutContentPathParam?.path)
    }
}

private class DiaryRepoFakeGitHubClient(
    private val contentResponse: ContentFile? = null
) : GitHubClient() {
    var putContentCallCount = 0
    var lastPutContentPathParam: GitHubContentApiPathParam? = null

    override suspend fun getContent(
        accessToken: GitHubPersonalAccessToken,
        pathParam: GitHubContentApiPathParam
    ): ContentFile? = contentResponse

    override suspend fun putContent(
        accessToken: GitHubPersonalAccessToken,
        pathParam: GitHubContentApiPathParam,
        message: String,
        content: String,
        sha: String?
    ) {
        putContentCallCount++
        lastPutContentPathParam = pathParam
    }
}

private class DiaryRepoFakeSettingRepository(
    private val loadResult: Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = null to null
) : SettingRepository(
    settingFilePath = kotlin.io.path.createTempFile(),
    gitHubClient = object : GitHubClient() {}
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult
}
