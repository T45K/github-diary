package io.github.t45k.githubDiary.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.onKeyPressed(applicableKeys: (KeyEvent) -> Boolean, action: () -> Unit): Modifier =
    onKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && applicableKeys(event)) {
            action()
            true
        } else {
            false
        }
    }

fun Modifier.onKeyWithCommandPressed(applicableKeys: (KeyEvent) -> Boolean, action: () -> Unit): Modifier =
    onKeyPressed({ it.isMetaPressed && applicableKeys(it) }, action)
