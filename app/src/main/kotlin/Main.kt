import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import core.time.DateProvider
import ui.AppScreen
import ui.AppViewModel

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "GitHub Diary") {
        val viewModel = remember { AppViewModel(DateProvider()) }
        MaterialTheme(colors = darkColors()) {
            Surface {
                AppScreen(viewModel = viewModel)
            }
        }
    }
}
