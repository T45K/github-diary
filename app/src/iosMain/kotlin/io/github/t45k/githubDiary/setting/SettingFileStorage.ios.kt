package io.github.t45k.githubDiary.setting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.fwrite

@OptIn(ExperimentalForeignApi::class)
class IosSettingFileStorage : SettingFileStorage {
    private val settingsPath: String = run {
        val paths = NSSearchPathForDirectoriesInDomains(
            directory = NSDocumentDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true
        )
        val directory = paths.firstOrNull() as? String
        if (directory.isNullOrBlank()) {
            "settings.json"
        } else {
            "$directory/settings.json"
        }
    }

    override suspend fun read(): String {
        return withContext(Dispatchers.Default) {
            val file = fopen(settingsPath, "r") ?: return@withContext ""
            try {
                if (fseek(file, 0, SEEK_END) != 0) {
                    return@withContext ""
                }
                val size = ftell(file).toInt()
                if (size <= 0) {
                    return@withContext ""
                }
                if (fseek(file, 0, SEEK_SET) != 0) {
                    return@withContext ""
                }
                val bytes = ByteArray(size)
                bytes.usePinned { pinned ->
                    fread(pinned.addressOf(0), 1u, size.toULong(), file)
                }
                bytes.decodeToString()
            } finally {
                fclose(file)
            }
        }
    }

    override suspend fun write(content: String) {
        withContext(Dispatchers.Default) {
            val file = fopen(settingsPath, "w") ?: return@withContext
            try {
                val bytes = content.encodeToByteArray()
                bytes.usePinned { pinned ->
                    fwrite(pinned.addressOf(0), 1u, bytes.size.toULong(), file)
                }
            } finally {
                fclose(file)
            }
        }
    }
}

actual fun createSettingFileStorage(): SettingFileStorage = IosSettingFileStorage()
