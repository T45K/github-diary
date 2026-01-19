package io.github.t45k.githubDiary.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

actual fun Modifier.onEnterKeyDown(onEnter: () -> Unit): Modifier {
    return this.onPreviewKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
            onEnter()
        }
        false
    }
}
