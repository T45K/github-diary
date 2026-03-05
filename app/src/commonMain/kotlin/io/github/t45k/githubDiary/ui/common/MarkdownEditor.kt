package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

@Composable
fun MarkdownEditor(
    text: String,
    onValueChange: (String) -> Unit,
) {
    val textFieldState = rememberTextFieldState(text)

    LaunchedEffect(textFieldState) {
        var previousText = text
        snapshotFlow { textFieldState.text }
            .collect { charSequence ->
                val newText = charSequence.toString()
                if (newText != previousText) {
                    applyMarkdownList(previousText, newText, textFieldState)
                    previousText = textFieldState.text.toString()
                    onValueChange(previousText)
                }
            }
    }

    OutlinedTextField(
        state = textFieldState,
        modifier = Modifier.fillMaxWidth(),
    )
}

internal fun applyMarkdownList(
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

    if (!lastLine.startsWith("- ")) return

    textFieldState.edit {
        if (lastLine.trim() == "-") {
            // Remove the empty list item "- "
            replace(lastLineStart, addedIndex, "")
            placeCursorAfterCharAt(lastLineStart)
        } else {
            // Add "- " prefix to the new line
            val insertPosition = addedIndex + 1
            replace(insertPosition, insertPosition, "- ")
            placeCursorAfterCharAt(insertPosition + 1)
        }
    }
}
