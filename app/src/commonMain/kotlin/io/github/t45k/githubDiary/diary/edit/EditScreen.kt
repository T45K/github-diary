package io.github.t45k.githubDiary.diary.edit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import io.github.t45k.githubDiary.ui.common.MarkdownEditor
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

@Composable
fun EditScreen(
    uiState: EditUiState,
    onBack: () -> Unit,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val canSave = when (uiState) {
        is EditUiState.Editing -> !uiState.isSaving
        else -> false
    }

    Scaffold(
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown &&
                event.isMetaPressed &&
                (event.key == Key.S || event.key == Key.Enter) &&
                canSave
            ) {
                onSave()
                true
            } else {
                false
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(uiState.date.format(LocalDate.Formats.ISO)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            when (uiState) {
                is EditUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is EditUiState.Editing -> {
                    MarkdownEditor(uiState.content, onContentChange)

                    Row(Modifier.padding(top = 12.dp)) {
                        Button(onClick = onSave, enabled = !uiState.isSaving) {
                            Text(if (uiState.isSaving) "Saving..." else "Save")
                        }
                    }
                }

                is EditUiState.Saved -> {
                    Text("Saved successfully!")
                }

                is EditUiState.Error -> {
                    Text(
                        text = "Error: ${uiState.message}",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    MarkdownEditor(
                        text = uiState.content,
                        onValueChange = onContentChange,
                    )

                    Row(Modifier.padding(top = 12.dp)) {
                        Button(onClick = onSave) { Text("Retry Save") }
                    }
                }
            }
        }
    }
}
