package io.github.t45k.githubDiary.ui.common

import androidx.compose.ui.Modifier

/**
 * Platform-specific key event detection for Enter key.
 * On Desktop: detects physical keyboard Enter key press.
 * On Android: returns unmodified Modifier (virtual keyboard doesn't emit key events).
 */
expect fun Modifier.onEnterKeyDown(onEnter: () -> Unit): Modifier
