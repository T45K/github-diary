package core.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GitHubRepositoryPathTest {

    @Test
    fun `invoke returns Right with valid path format`() {
        val result = GitHubRepositoryPath("owner/repo")

        assertTrue(result.isRight())
        result.onRight {
            assertEquals("owner", it.owner)
            assertEquals("repo", it.name)
        }
    }

    @Test
    fun `invoke returns Left with invalid path format - no slash`() {
        val result = GitHubRepositoryPath("invalidpath")

        assertTrue(result.isLeft())
    }

    @Test
    fun `invoke returns Left with invalid path format - too many slashes`() {
        val result = GitHubRepositoryPath("owner/repo/extra")

        assertTrue(result.isLeft())
    }

    @Test
    fun `invoke returns Left with empty string`() {
        val result = GitHubRepositoryPath("")

        assertTrue(result.isLeft())
    }

    @Test
    fun `toString returns owner slash name format`() {
        val path = GitHubRepositoryPath("myowner", "myrepo")

        assertEquals("myowner/myrepo", path.toString())
    }
}
