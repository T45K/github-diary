package io.github.t45k.githubDiary.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun MarkdownEditor(
    key: Any,
    initialText: String,
    updateText: (String) -> Unit,
) {
    var textFieldValue by remember(key) {
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }

    LaunchedEffect(initialText) {
        if (textFieldValue.text != initialText && textFieldValue.composition == null) {
            textFieldValue = textFieldValue.copy(
                text = initialText,
                selection = TextRange(initialText.length),
            )
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            val oldText = textFieldValue.text
            val newText = newTextFieldValue.text

            val isNewlineInserted = newText.length == oldText.length + 1 &&
                newText.endsWith("\n") &&
                !oldText.endsWith("\n")

            val processedTextFieldValue = if (isNewlineInserted && newTextFieldValue.composition == null) {
                when {
                    newText.endsWith("- \n") -> {
                        val text = newText.dropLast(3)
                        newTextFieldValue.copy(text = text, selection = TextRange(text.length))
                    }

                    newText.endsWith("\n") -> {
                        val lastLine = newText.dropLast(1).substringAfterLast("\n")
                        if (lastLine.matches(Regex("""^- .+"""))) {
                            val text = "$newText- "
                            newTextFieldValue.copy(text = text, selection = TextRange(text.length))
                        } else {
                            newTextFieldValue
                        }
                    }

                    else -> newTextFieldValue
                }
            } else {
                newTextFieldValue
            }

            textFieldValue = processedTextFieldValue
            if (processedTextFieldValue.composition == null && processedTextFieldValue.text != initialText) {
                updateText(processedTextFieldValue.text)
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
