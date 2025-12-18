package ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.auth.AuthMode

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onSaved: () -> Unit = {}) {
    var token by remember(viewModel.state.token) { mutableStateOf(viewModel.state.token) }
    var repo by remember(viewModel.state.repo) { mutableStateOf(viewModel.state.repo) }
    var mode by remember(viewModel.state.authMode) { mutableStateOf(viewModel.state.authMode) }

    Column(Modifier.padding(16.dp)) {
        Text("Authentication Mode")
        AuthMode.values().forEach { m ->
            ModeRow(selected = mode == m, label = m.name) {
                mode = m
                viewModel.updateMode(m)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Token")
        OutlinedTextField(
            value = token,
            onValueChange = {
                token = it
                viewModel.updateToken(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Text("Repo (org/repo)")
        OutlinedTextField(
            value = repo,
            onValueChange = {
                repo = it
                viewModel.updateRepo(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            viewModel.save { _, _ ->
                onSaved()
            }
        }, enabled = !viewModel.state.isSaving) {
            Text(if (viewModel.state.isSaving) "Saving..." else "Save")
        }

        viewModel.state.message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it)
        }
    }
}

@Composable
private fun ModeRow(selected: Boolean, label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 4.dp))
    }
}
