package io.github.t45k.githubDiary.setting

/**
 * Platform-specific file storage for settings.
 * Handles reading and writing settings JSON to persistent storage.
 */
interface SettingFileStorage {
    /**
     * Read settings content from storage.
     * Returns empty string if file doesn't exist.
     */
    suspend fun read(): String

    /**
     * Write settings content to storage.
     */
    suspend fun write(content: String)
}

/**
 * Factory function to create platform-specific SettingFileStorage.
 */
expect fun createSettingFileStorage(): SettingFileStorage
