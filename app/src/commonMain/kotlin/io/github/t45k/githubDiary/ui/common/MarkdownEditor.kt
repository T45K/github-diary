package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun MarkdownEditor(
    text: String,
    onValueChange: (String) -> Unit,
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text, TextRange(text.length)))
    }

    if (textFieldValue.text != text && textFieldValue.composition == null) {
        textFieldValue = textFieldValue.copy(text = text)
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val oldValue = textFieldValue
            val processedValue = handleMarkdownList(oldValue, newValue)

            textFieldValue = processedValue
            if (processedValue.text != oldValue.text) {
                onValueChange(processedValue.text)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}


internal fun handleMarkdownList(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    val selectionLength = oldValue.selection.max - oldValue.selection.min
    if (newValue.composition == null && newValue.text.length == oldValue.text.length - selectionLength + 1) {
        val addedIndex = newValue.selection.start - 1
        if (addedIndex >= 0 && addedIndex < newValue.text.length && newValue.text[addedIndex] == '\n') {
            val textBeforeNewline = newValue.text.substring(0, addedIndex)
            val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
            val lastLine = textBeforeNewline.substring(lastLineStart)

            if (lastLine.startsWith("- ")) {
                if (lastLine.trim() == "-") {
                    // Case 2: Line was just "- ", remove it
                    val newText = newValue.text.substring(0, lastLineStart) + newValue.text.substring(addedIndex)
                    val newSelection = TextRange(lastLineStart + 1)
                    return TextFieldValue(newText, newSelection)
                } else {
                    // Case 1: Line started with "- ", add it to next line
                    val prefix = "- "
                    val newText =
                        newValue.text.substring(0, addedIndex + 1) + prefix + newValue.text.substring(addedIndex + 1)
                    val newSelection = TextRange(addedIndex + 1 + prefix.length)
                    return TextFieldValue(newText, newSelection)
                }
            }
        }
    }
    return newValue
}
