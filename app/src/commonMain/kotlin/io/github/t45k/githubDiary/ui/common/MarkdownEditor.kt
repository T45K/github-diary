package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun MarkdownEditor(
    textFieldState: TextFieldState,
) {
    LaunchedEffect(textFieldState) {
        var previousText = textFieldState.text.toString()
        snapshotFlow { textFieldState.text }
            .collect { charSequence ->
                val newText = charSequence.toString()
                if (newText != previousText) {
                    applyMarkdownContinuation(previousText, newText, textFieldState)
                    previousText = textFieldState.text.toString()
                }
            }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        state = textFieldState,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
    )
}

// Regex patterns for markdown continuation markers
private val BULLET_LIST_REGEX = Regex("""^(\s*)([-*+])(\s)(\[[ xX]]\s)?(.*)$""")
private val ORDERED_LIST_REGEX = Regex("""^(\s*)(\d+)([.)]\s)(.*)$""")
private val BLOCKQUOTE_PREFIX_REGEX = Regex("""^((?:\s*>\s?)+)(.*)$""")

internal fun applyMarkdownContinuation(
    oldText: String,
    newText: String,
    textFieldState: TextFieldState,
) {
    // Find the first differing position to locate the added character
    var addedIndex = -1
    for (i in newText.indices) {
        if (i >= oldText.length || newText[i] != oldText[i]) {
            addedIndex = i
            break
        }
    }
    if (addedIndex == -1 || newText[addedIndex] != '\n') return

    val textBeforeNewline = newText.substring(0, addedIndex)
    val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
    val lastLine = textBeforeNewline.substring(lastLineStart)

    val continuation = buildContinuation(lastLine) ?: return

    textFieldState.edit {
        if (continuation.isEmpty) {
            // Empty marker -> remove the marker line (including the newline after it)
            replace(lastLineStart, addedIndex + 1, "")
            placeCursorAfterCharAt(lastLineStart - 1)
        } else {
            val insertPosition = addedIndex + 1
            val prefix = continuation.prefix
            replace(insertPosition, insertPosition, prefix)
            placeCursorAfterCharAt(insertPosition + prefix.length - 1)
        }
    }
}

private data class Continuation(
    val prefix: String,
    val isEmpty: Boolean,
)

private fun buildContinuation(line: String): Continuation? {
    // First, check if the line starts with a blockquote prefix
    val blockquoteMatch = BLOCKQUOTE_PREFIX_REGEX.matchEntire(line)
    if (blockquoteMatch != null) {
        val quotePrefix = blockquoteMatch.groupValues[1]
        val innerContent = blockquoteMatch.groupValues[2]

        // Check if the inner content has a list marker
        val innerContinuation = buildListContinuation(innerContent)
        if (innerContinuation != null) {
            return Continuation(
                prefix = quotePrefix + innerContinuation.prefix,
                isEmpty = innerContinuation.isEmpty,
            )
        }

        // Pure blockquote (no inner list)
        return if (innerContent.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else {
            Continuation(prefix = quotePrefix, isEmpty = false)
        }
    }

    // No blockquote, try list patterns directly
    return buildListContinuation(line)
}

private fun buildListContinuation(line: String): Continuation? {
    // Try bullet list pattern (including checklists): "  - [x] content"
    BULLET_LIST_REGEX.matchEntire(line)?.let { match ->
        val indent = match.groupValues[1]
        val marker = match.groupValues[2]
        val space = match.groupValues[3]
        val checkbox = match.groupValues[4] // e.g. "[x] " or ""
        val content = match.groupValues[5]

        return if (content.isEmpty() && checkbox.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else if (content.isEmpty() && checkbox.isNotEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else if (checkbox.isNotEmpty()) {
            Continuation(prefix = "$indent$marker$space[ ] ", isEmpty = false)
        } else {
            Continuation(prefix = "$indent$marker$space", isEmpty = false)
        }
    }

    // Try ordered list pattern: "  1. content" or "  1) content"
    ORDERED_LIST_REGEX.matchEntire(line)?.let { match ->
        val indent = match.groupValues[1]
        val number = match.groupValues[2].toInt()
        val delimiter = match.groupValues[3] // ". " or ") "
        val content = match.groupValues[4]

        return if (content.isEmpty()) {
            Continuation(prefix = "", isEmpty = true)
        } else {
            Continuation(
                prefix = "$indent${number + 1}$delimiter",
                isEmpty = false,
            )
        }
    }

    return null
}
