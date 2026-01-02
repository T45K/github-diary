import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.appModule
import org.koin.core.context.startKoin
import ui.AppScreen

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
