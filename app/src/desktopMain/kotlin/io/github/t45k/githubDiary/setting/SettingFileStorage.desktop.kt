package io.github.t45k.githubDiary.setting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class DesktopSettingFileStorage : SettingFileStorage {
    private val settingFilePath = Path(System.getenv("HOME"), ".github_diary", "settings.json")

    init {
        settingFilePath.createParentDirectories()
        if (settingFilePath.notExists()) {
            settingFilePath.createFile()
        }
    }

    override suspend fun read(): String {
        return withContext(Dispatchers.IO) {
            settingFilePath.readText()
        }
    }

    override suspend fun write(content: String) {
        withContext(Dispatchers.IO) {
            settingFilePath.writeText(content, options = arrayOf(WRITE, TRUNCATE_EXISTING))
        }
    }
}

actual fun createSettingFileStorage(): SettingFileStorage = DesktopSettingFileStorage()
