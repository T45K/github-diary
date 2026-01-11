package io.github.t45k.githubDiary.core.entity

import io.github.t45k.githubDiary.core.entity.GitHubRepositoryPath
import org.junit.jupiter.api.Test

class GitHubRepositoryPathTest {

    @Test
    fun `invoke returns Right with valid path format`() {
        // given
        val pathString = "owner/repo"

        // when
        val result = GitHubRepositoryPath(pathString)

        // then
        assert(result.isRight())
        result.onRight {
            assert(it.owner == "owner")
            assert(it.name == "repo")
        }
    }

    @Test
    fun `invoke returns Left with invalid path format - no slash`() {
        // given
        val pathString = "invalidpath"

        // when
        val result = GitHubRepositoryPath(pathString)

        // then
        assert(result.isLeft())
    }

    @Test
    fun `invoke returns Left with invalid path format - too many slashes`() {
        // given
        val pathString = "owner/repo/extra"

        // when
        val result = GitHubRepositoryPath(pathString)

        // then
        assert(result.isLeft())
    }

    @Test
    fun `invoke returns Left with empty string`() {
        // given
        val pathString = ""

        // when
        val result = GitHubRepositoryPath(pathString)

        // then
        assert(result.isLeft())
    }

    @Test
    fun `toString returns owner slash name format`() {
        // given
        val path = GitHubRepositoryPath("myowner", "myrepo")

        // when
        val result = path.toString()

        // then
        assert(result == "myowner/myrepo")
    }
}
