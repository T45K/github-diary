package io.github.t45k.githubDiary.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

fun Modifier.onKeyPressed(applicableKeys: (KeyEvent) -> Boolean, action: (event: KeyEvent) -> Unit): Modifier =
    onKeyEvent { event ->
        if (event.type == KeyEventType.KeyDown && applicableKeys(event)) {
            action(event)
            true
        } else {
            false
        }
    }

fun Modifier.onKeyWithCommandPressed(applicableKeys: (KeyEvent) -> Boolean, action: (event: KeyEvent) -> Unit): Modifier =
    onKeyPressed({ it.isMetaPressed && applicableKeys(it) }, action)
