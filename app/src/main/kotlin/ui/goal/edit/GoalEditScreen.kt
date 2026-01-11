package ui.goal.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

@Composable
fun GoalEditScreen(
    uiState: GoalEditUiState,
    goBack: () -> Unit,
    completeGoal: (index: Int) -> Unit,
    incompleteGoal: (index: Int) -> Unit,
    updateGoalContent: (index: Int, goal: String) -> Unit,
    removeGoal: (index: Int) -> Unit,
    addGoal: () -> Unit,
    syncLastMoney: () -> Unit,
    updateLastMoney: (amount: Int) -> Unit,
    updateFrontMoney: (amount: Int) -> Unit,
    updateBackMoney: (amount: Int) -> Unit,
    save: () -> Unit,
) {
    val canSave = when (uiState) {
        is GoalEditUiState.Editing -> !uiState.isSaving
//        is GoalEditUiState.Error -> !uiState.isSaving
        else -> false
    }

    Scaffold(
        modifier = Modifier.onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown &&
                event.isMetaPressed &&
                (event.key == Key.S || event.key == Key.Enter) &&
                canSave
            ) {
                save()
                true
            } else {
                false
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(uiState.yearMonth.format(YearMonth.Format { year(); char('/'); monthNumber() })) },
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(scrollState)) {
            when (uiState) {
                is GoalEditUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is GoalEditUiState.Editing -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Goals",
                            style = MaterialTheme.typography.h5,
                        )
                        uiState.goal.goals.forEachIndexed { index, (goal, isCompleted) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isCompleted) {
                                    Checkbox(checked = true, onCheckedChange = { incompleteGoal(index) })
                                } else {
                                    Checkbox(checked = false, onCheckedChange = { completeGoal(index) })
                                }
                                Spacer(Modifier.padding(start = 8.dp))
                                OutlinedTextField(
                                    value = goal,
                                    onValueChange = { updateGoalContent(index, it) },
                                    modifier = Modifier.weight(1f),
                                )
                                IconButton(onClick = { removeGoal(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colors.error,
                                    )
                                }
                            }
                        }
                        Button(onClick = addGoal) { Icon(Icons.Default.Add, contentDescription = "Add") }
                    }

                    Spacer(Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Money",
                            style = MaterialTheme.typography.h5,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Last ", modifier = Modifier.width(60.dp))
                            OutlinedTextField(
                                value = uiState.goal.moneyInfo.last.toString(),
                                onValueChange = { input -> input.toIntOrNull()?.let { updateLastMoney(it) } },
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { syncLastMoney() }) { Icon(Icons.Default.Refresh, contentDescription = "reload") }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Front ", modifier = Modifier.width(60.dp))
                            OutlinedTextField(
                                value = uiState.goal.moneyInfo.front.toString(),
                                onValueChange = { input -> input.toIntOrNull()?.let { updateFrontMoney(it) } },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Back ", modifier = Modifier.width(60.dp))
                            OutlinedTextField(
                                value = uiState.goal.moneyInfo.back.toString(),
                                onValueChange = { input -> input.toIntOrNull()?.let { updateBackMoney(it) } },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total ", modifier = Modifier.width(60.dp))
                            OutlinedTextField(
                                value = uiState.goal.moneyInfo.total.toString(),
                                onValueChange = { },
                                enabled = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Diff ", modifier = Modifier.width(60.dp))
                            OutlinedTextField(
                                value = uiState.goal.moneyInfo.diff.toString(),
                                onValueChange = { },
                                enabled = false,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Row(Modifier.padding(top = 12.dp)) {
                        Button(onClick = save, enabled = canSave) {
                            Text(if (uiState.isSaving) "Saving..." else "Save")
                        }
                    }
                }

                is GoalEditUiState.Error -> {
                }
            }
        }
    }
}
