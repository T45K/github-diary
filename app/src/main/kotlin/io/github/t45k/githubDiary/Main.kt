package io.github.t45k.githubDiary

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.t45k.githubDiary.di.appModule
import io.github.t45k.githubDiary.ui.AppScreen
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        Window(onCloseRequest = ::exitApplication, title = "GitHub Diary") {
            MaterialTheme(colors = darkColors()) {
                Surface {
                    AppScreen()
                }
            }
        }
    }
}
