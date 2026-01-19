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
    key: Any,
    initialText: String,
    updateText: (String) -> Unit,
) {
    var textFieldValue by remember(key) {
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }
    var lastKeyWasEnter by remember(key) { mutableStateOf(false) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
            val newText = newTextFieldValue.text
            val processedText = if (lastKeyWasEnter) {
                when {
                    newText.endsWith("- \n") -> newText.dropLast(3)
                    newText.endsWith("\n") -> {
                        val lastLine = newText.dropLast(1).substringAfterLast("\n")
                        if (lastLine.matches(Regex("""^- .+"""))) "$newText- " else newText
                    }

                    else -> newText
                }
            } else {
                newText
            }

            lastKeyWasEnter = false

            textFieldValue =
                if (processedText == newText) newTextFieldValue
                else TextFieldValue(
                    text = processedText,
                    selection = TextRange(processedText.length),
                )

            updateText(textFieldValue.text)
        },
        modifier = Modifier
            .fillMaxWidth()
            .onEnterKeyDown { lastKeyWasEnter = true },
    )
}
