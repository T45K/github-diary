package core.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GitHubPersonalAccessTokenTest {

    @Test
    fun `value class stores token value`() {
        val token = GitHubPersonalAccessToken("ghp_xxxxxxxxxxxx")

        assertEquals("ghp_xxxxxxxxxxxx", token.value)
    }

    @Test
    fun `two tokens with same value are equal`() {
        val token1 = GitHubPersonalAccessToken("ghp_test")
        val token2 = GitHubPersonalAccessToken("ghp_test")

        assertEquals(token1, token2)
    }

    @Test
    fun `token can be empty string`() {
        val token = GitHubPersonalAccessToken("")

        assertEquals("", token.value)
    }
}
