package io.github.t45k.githubDiary.search

import kotlinx.datetime.LocalDate

data class SearchResult(
    val totalCount: Int,
    val items: List<SearchResultEntry>,
    val hasMore: Boolean,
)

data class SearchResultEntry(
    val path: String,
    val date: LocalDate?,
    val fragments: List<String>,
)

/**
 * Parse diary file path format "{year}/{month}/{day}/README.md" into LocalDate.
 * Returns null for non-matching paths (e.g., goal files "{year}/{month}/README.md").
 */
fun parseDateFromPath(path: String): LocalDate? {
    val parts = path.split("/")
    if (parts.size != 4 || parts[3] != "README.md") return null
    return try {
        LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    } catch (_: Exception) {
        null
    }
}
