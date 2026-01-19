package io.github.t45k.githubDiary.setting

import io.github.t45k.githubDiary.AndroidContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidSettingFileStorage : SettingFileStorage {
    private val settingFile: File by lazy {
        File(AndroidContextProvider.applicationContext.filesDir, "settings.json").also { file ->
            if (!file.exists()) {
                file.createNewFile()
            }
        }
    }

    override suspend fun read(): String {
        return withContext(Dispatchers.IO) {
            if (settingFile.exists()) {
                settingFile.readText()
            } else {
                ""
            }
        }
    }

    override suspend fun write(content: String) {
        withContext(Dispatchers.IO) {
            settingFile.writeText(content)
        }
    }
}

actual fun createSettingFileStorage(): SettingFileStorage = AndroidSettingFileStorage()
