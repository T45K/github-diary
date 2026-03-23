package io.github.t45k.githubDiary

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.window.ComposeUIViewController
import io.github.t45k.githubDiary.ui.AppScreen
import org.koin.core.context.startKoin
import org.koin.core.error.KoinApplicationAlreadyStartedException
import platform.UIKit.UIViewController

/**
 * Starts Koin DI if not already started.
 * KoinApplicationAlreadyStartedException can occur because SwiftUI's lifecycle
 * may recreate the UIViewController multiple times (e.g., on scene phase changes
 * or configuration changes), causing MainViewController() to be called again
 * while Koin is already initialized from a previous invocation in the same process.
 */
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
