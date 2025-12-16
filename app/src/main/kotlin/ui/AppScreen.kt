package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.navigation.NavRoute

@Composable
fun AppScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("GitHub Diary") })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            HeaderActions(
                onToday = { viewModel.goToday() },
                onSync = { /* sync to be implemented */ },
                onSettings = { viewModel.navigate(NavRoute.Settings.name) }
            )

            Text("Route: ${state.currentRoute}")
            Text("Selected date: ${state.selectedDate}")
            if (state.isLoading) {
                Text("Loading...")
            }
            state.errorMessage?.let { Text("Error: $it") }
        }
    }
}

@Composable
private fun HeaderActions(
    onToday: () -> Unit,
    onSync: () -> Unit,
    onSettings: () -> Unit
) {
    Row(Modifier.padding(bottom = 12.dp)) {
        Button(onClick = onToday) { Text("Today") }
        Button(onClick = onSync, modifier = Modifier.padding(start = 8.dp)) { Text("Sync") }
        Button(onClick = onSettings, modifier = Modifier.padding(start = 8.dp)) { Text("Settings") }
    }
}