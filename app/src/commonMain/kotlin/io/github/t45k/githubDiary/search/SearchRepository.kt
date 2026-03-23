package io.github.t45k.githubDiary.search

import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.setting.SettingRepository

open class SearchRepository(
    private val client: GitHubClient,
    private val settingRepository: SettingRepository,
) {
    open suspend fun search(query: String, page: Int = 1, perPage: Int = 30): SearchResult {
        val (accessToken, repoPath) = settingRepository.load()
        if (accessToken == null || repoPath == null) {
            return SearchResult(totalCount = 0, items = emptyList(), hasMore = false)
        }

        val response = client.searchCode(accessToken, query, repoPath.owner, repoPath.name, page, perPage)
            ?: return SearchResult(totalCount = 0, items = emptyList(), hasMore = false)

        val entries = response.items.map { item ->
            SearchResultEntry(
                path = item.path,
                date = parseDateFromPath(item.path),
                fragments = item.textMatches.map { it.fragment },
            )
        }
        return SearchResult(
            totalCount = response.totalCount,
            items = entries,
            hasMore = page * perPage < response.totalCount,
        )
    }
}
