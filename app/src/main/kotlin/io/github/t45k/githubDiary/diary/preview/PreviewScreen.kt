package io.github.t45k.githubDiary.diary.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

@Composable
fun PreviewScreen(
    uiState: PreviewUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.date.format(LocalDate.Formats.ISO)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (uiState) {
                is PreviewUiState.Loading -> Text("Loading...")
                is PreviewUiState.NotFound -> Text("Not found. Tap edit to start writing.")
                is PreviewUiState.Error -> Text("Error: ${uiState.message}", color = MaterialTheme.colors.error)
                is PreviewUiState.Success -> Text(uiState.content)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEdit) { Text("Edit") }
        }
    }
}
