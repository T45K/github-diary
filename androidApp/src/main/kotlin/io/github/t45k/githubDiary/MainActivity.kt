package io.github.t45k.githubDiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import io.github.t45k.githubDiary.ui.AppScreen
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AndroidContextProvider.init(this)

        startKoin {
            modules(appModule)
        }

        setContent {
            MaterialTheme(colors = darkColors()) {
                Surface {
                    AppScreen()
                }
            }
        }
    }
}
