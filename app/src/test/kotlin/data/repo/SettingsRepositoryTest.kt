package data.repo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class SettingsRepositoryTest {

    @Test
    fun `invalid format is rejected`() {
        val repo = SettingsRepository(tempFile())

        val result = repo.save("invalid")

        assertTrue(result is core.model.Result.Failure)
    }

    @Test
    fun `save and load roundtrip`() {
        val path = tempFile()
        val repo = SettingsRepository(path)

        val saved = repo.save("owner/repo")
        assertTrue(saved is core.model.Result.Success)

        val loaded = repo.load()
        assertEquals("owner/repo", (loaded as core.model.Result.Success).value)
    }

    @Test
    fun `clear deletes file`() {
        val path = tempFile()
        val repo = SettingsRepository(path)
        repo.save("owner/repo")

        repo.clear()

        assertFalse(Files.exists(path))
    }

    private fun tempFile(): Path {
        val dir = Files.createTempDirectory("settingsRepoTest")
        return dir.resolve("repo.txt")
    }
}
