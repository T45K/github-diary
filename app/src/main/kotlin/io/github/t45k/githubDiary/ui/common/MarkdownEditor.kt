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
    initialText: String,
    updateText: (String) -> Unit,
) {
    var textFieldValue by remember(initialText) {
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            val newText = newTextFieldValue.text
            val processedText = when {
                newText.endsWith("- \n") -> newText.removeRange(newText.length - 3, newText.length)
                newText.contains(Regex("""^-\s.+\n$""", RegexOption.MULTILINE)) -> "$newText- "
                else -> newText
            }

            textFieldValue =
                if (processedText == newText) newTextFieldValue
                else TextFieldValue(
                    text = processedText,
                    selection = TextRange(processedText.length),
                )

            updateText(textFieldValue.text)
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
