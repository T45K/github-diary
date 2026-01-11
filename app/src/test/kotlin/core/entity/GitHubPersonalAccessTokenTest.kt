package core.entity

import org.junit.jupiter.api.Test

class GitHubPersonalAccessTokenTest {

    @Test
    fun `value class stores token value`() {
        // given
        val tokenValue = "ghp_xxxxxxxxxxxx"

        // when
        val token = GitHubPersonalAccessToken(tokenValue)

        // then
        assert(token.value == "ghp_xxxxxxxxxxxx")
    }

    @Test
    fun `two tokens with same value are equal`() {
        // given
        val token1 = GitHubPersonalAccessToken("ghp_test")
        val token2 = GitHubPersonalAccessToken("ghp_test")

        // when
        val areEqual = token1 == token2

        // then
        assert(areEqual)
    }

    @Test
    fun `token can be empty string`() {
        // given
        val emptyValue = ""

        // when
        val token = GitHubPersonalAccessToken(emptyValue)

        // then
        assert(token.value == "")
    }
}
