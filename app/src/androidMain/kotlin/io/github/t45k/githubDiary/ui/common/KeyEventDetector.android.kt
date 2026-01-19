package io.github.t45k.githubDiary.ui.common

import androidx.compose.ui.Modifier

/**
 * On Android, virtual keyboard doesn't emit key events,
 * so we simply return the unmodified Modifier.
 */
actual fun Modifier.onEnterKeyDown(onEnter: () -> Unit): Modifier {
    return this
}
