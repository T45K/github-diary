package ui.goal.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

@Composable
fun GoalPreviewScreen(
    uiState: GoalPreviewUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.yearMonth.format(YearMonth.Format { year(); char('/'); monthNumber() })) },
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
                is GoalPreviewUiState.Loading -> Text("Loading...")
                is GoalPreviewUiState.Error -> Text("Error: ${uiState.message}", color = MaterialTheme.colors.error)
                is GoalPreviewUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Goals",
                            style = MaterialTheme.typography.h5,
                        )
                        uiState.content.goals.forEach { (goal, isCompleted) ->
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                if (isCompleted) {
                                    Icon(Icons.Default.CheckBox, contentDescription = "completed")
                                } else {
                                    Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = "incompleted")
                                }
                                Spacer(Modifier.padding(start = 8.dp))
                                Text(text = goal)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Money",
                            style = MaterialTheme.typography.h5,
                        )
                        Text(
                            text = uiState.content.moneyInfo.content(),
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEdit) { Text("Edit") }
        }
    }
}
