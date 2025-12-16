import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.runtime.remember
import ui.AppScreen
import ui.AppViewModel
import core.time.DateProvider

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