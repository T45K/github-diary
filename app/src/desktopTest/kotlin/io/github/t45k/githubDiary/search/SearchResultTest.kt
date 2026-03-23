package io.github.t45k.githubDiary.search

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test

class SearchResultTest {

    @Test
    fun `parseDateFromPath parses valid diary path`() {
        val result = parseDateFromPath("2026/01/02/README.md")
        assert(result == LocalDate(2026, 1, 2))
    }

    @Test
    fun `parseDateFromPath parses end of year date`() {
        val result = parseDateFromPath("2026/12/31/README.md")
        assert(result == LocalDate(2026, 12, 31))
    }

    @Test
    fun `parseDateFromPath returns null for goal path`() {
        val result = parseDateFromPath("2026/01/README.md")
        assert(result == null)
    }

    @Test
    fun `parseDateFromPath returns null for malformed path`() {
        val result = parseDateFromPath("invalid/path")
        assert(result == null)
    }

    @Test
    fun `parseDateFromPath returns null for non-README file`() {
        val result = parseDateFromPath("2026/01/02/other.md")
        assert(result == null)
    }

    @Test
    fun `parseDateFromPath returns null for invalid date`() {
        val result = parseDateFromPath("2026/13/32/README.md")
        assert(result == null)
    }
}
