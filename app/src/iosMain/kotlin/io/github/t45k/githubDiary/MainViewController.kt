package io.github.t45k.githubDiary

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.window.ComposeUIViewController
import io.github.t45k.githubDiary.ui.AppScreen
import org.koin.core.context.startKoin
import org.koin.core.error.KoinApplicationAlreadyStartedException
import platform.UIKit.UIViewController

private fun startKoinIfNeeded() {
    try {
        startKoin {
            modules(appModule)
        }
    } catch (_: KoinApplicationAlreadyStartedException) {
        // Already started in this process.
    }
}

fun MainViewController(): UIViewController {
    startKoinIfNeeded()
    return ComposeUIViewController {
        MaterialTheme(colors = darkColors()) {
            Surface {
                AppScreen()
            }
        }
    }
}
