package io.github.t45k.githubDiary.core.repository

import io.github.t45k.githubDiary.core.entity.DiaryContent
import io.github.t45k.githubDiary.core.entity.GitHubPersonalAccessToken
import io.github.t45k.githubDiary.core.entity.GitHubRepositoryPath
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import kotlin.io.encoding.Base64
import kotlin.io.path.createTempFile

class DiaryRepositoryTest {

    @Test
    fun `findByDate returns default DiaryContent when no credentials`() = runTest {
        // given
        val settingRepo = DiaryRepoFakeSettingRepository(loadResult = null to null)
        val gitHubClient = DiaryRepoFakeGitHubClient()
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)
        val date = LocalDate(2026, 1, 2)

        // when
        val result = diaryRepository.findByDate(date)

        // then
        assert(result.date == date)
        assert(result.content == DiaryContent.init(date).content)
    }

    @Test
    fun `findByDate returns default DiaryContent when content not found`() = runTest {
        // given
        val settingRepo = DiaryRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = DiaryRepoFakeGitHubClient(contentResponse = null)
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)
        val date = LocalDate(2026, 1, 2)

        // when
        val result = diaryRepository.findByDate(date)

        // then
        assert(result.date == date)
        assert(result.content == DiaryContent.init(date).content)
    }

    @Test
    fun `findByDate returns DiaryContent from repository`() = runTest {
        // given
        val expectedContent = "# 2026/01/02 (Fri)\n\nDiary entry"
        val encodedContent = Base64.encode(expectedContent.toByteArray())
        val settingRepo = DiaryRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = DiaryRepoFakeGitHubClient(contentResponse = ContentFile("sha123", encodedContent))
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)
        val date = LocalDate(2026, 1, 2)

        // when
        val result = diaryRepository.findByDate(date)

        // then
        assert(result.date == date)
        assert(result.content == expectedContent)
    }

    @Test
    fun `save does nothing when no credentials`() = runTest {
        // given
        val settingRepo = DiaryRepoFakeSettingRepository(loadResult = null to null)
        val gitHubClient = DiaryRepoFakeGitHubClient()
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)
        val diary = DiaryContent(LocalDate(2026, 1, 2), "content")

        // when
        diaryRepository.save(diary)

        // then
        assert(gitHubClient.putContentCallCount == 0)
    }

    @Test
    fun `save calls putContent when credentials exist`() = runTest {
        // given
        val settingRepo = DiaryRepoFakeSettingRepository(
            loadResult = GitHubPersonalAccessToken("token") to GitHubRepositoryPath("owner", "repo")
        )
        val gitHubClient = DiaryRepoFakeGitHubClient()
        val diaryRepository = DiaryRepository(gitHubClient, settingRepo)
        val diary = DiaryContent(LocalDate(2026, 1, 2), "content")

        // when
        diaryRepository.save(diary)

        // then
        assert(gitHubClient.putContentCallCount == 1)
        assert(gitHubClient.lastPutContentPathParam != null)
        assert(gitHubClient.lastPutContentPathParam?.owner == "owner")
        assert(gitHubClient.lastPutContentPathParam?.repo == "repo")
        assert(gitHubClient.lastPutContentPathParam?.path == "2026/01/02/README.md")
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
    settingFilePath = createTempFile(),
    gitHubClient = object : GitHubClient() {}
) {
    override suspend fun load(): Pair<GitHubPersonalAccessToken?, GitHubRepositoryPath?> = loadResult
}
