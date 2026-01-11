package io.github.t45k.githubDiary.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onTokenChange: (String) -> Unit,
    onRepoChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    when (uiState) {
        is SettingsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SettingsUiState.Ready -> {
            var token by remember(uiState.token) { mutableStateOf(uiState.token) }
            var repo by remember(uiState.repo) { mutableStateOf(uiState.repo) }

            Column(Modifier.padding(16.dp)) {
                Spacer(Modifier.height(12.dp))
                Text("Token")
                OutlinedTextField(
                    value = token,
                    onValueChange = {
                        token = it
                        onTokenChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))
                Text("Repo (org/repo)")
                OutlinedTextField(
                    value = repo,
                    onValueChange = {
                        repo = it
                        onRepoChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save")
                }

                uiState.message?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it)
                }
            }
        }
    }
}
