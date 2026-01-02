import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.AppScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "GitHub Diary") {
        MaterialTheme(colors = darkColors()) {
            Surface {
                AppScreen()
            }
        }
    }
}
