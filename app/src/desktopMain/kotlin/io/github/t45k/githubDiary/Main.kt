package io.github.t45k.githubDiary

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.t45k.githubDiary.ui.AppScreen
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        val windowState = rememberWindowState(width = 900.dp, height = 700.dp)
        Window(onCloseRequest = ::exitApplication, title = "GitHub Diary", state = windowState) {
            MenuBar {
                Menu("ファイル") {
                    Item("今日の日記", onClick = { /* handled by app */ }, shortcut = KeyShortcut(Key.T, meta = true))
                }
            }
            MaterialTheme(colors = darkColors()) {
                Surface {
                    AppScreen()
                }
            }
        }
    }
}
