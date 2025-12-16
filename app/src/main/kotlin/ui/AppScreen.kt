package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ui.navigation.NavRoute

@Composable
fun AppScreen(viewModel: AppViewModel, onSync: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Button(onClick = {
                            viewModel.goToCalendarToday()
                        }) { Text("今日") }
                    }
                    Row {
                        IconButton(onClick = onSync) { Icon(Icons.Default.Refresh, contentDescription = "Sync") }
                        IconButton(onClick = { viewModel.navigate(NavRoute.Settings.name) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Route: ${state.currentRoute}")
            Text("Selected date: ${state.selectedDate}")
            if (state.isLoading) {
                Text("Loading...")
            }
            state.errorMessage?.let { Text("Error: $it") }
        }
    }
}