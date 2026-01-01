package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.repository.CalendarRepository
import core.repository.SettingRepository
import core.time.DateProvider
import kotlinx.datetime.number
import ui.calendar.CalendarScreen
import ui.calendar.CalendarViewModel
import ui.edit.EditScreen
import ui.edit.EditViewModel
import ui.navigation.NavRoute
import ui.preview.PreviewScreen
import ui.preview.PreviewViewModel
import ui.settings.SettingsScreen
import ui.settings.SettingsViewModel

@Composable
fun AppScreen(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsState()

    // シンプルなremember生成（本実装ではDIへ置換予定）
    val ghClient = remember { core.repository.GitHubClient() }
    val settingRepo = remember { SettingRepository(gitHubClient = ghClient) }
    val calendarRepository = remember { CalendarRepository(ghClient, settingRepo) }
    val diaryRepository = remember { core.repository.DiaryRepository(ghClient, settingRepo) }
    val dateProvider = remember { DateProvider() }
    val settingsVm = remember { SettingsViewModel(settingRepo) }
    val calendarVm = remember { CalendarViewModel(calendarRepository, dateProvider.today().year, dateProvider.today().month.number) }
    val previewVm = remember { PreviewViewModel(diaryRepository, state.selectedDate ?: dateProvider.today()) }
    val editVm = remember { EditViewModel(diaryRepository, dateProvider) }

    Scaffold(
        topBar = {
            TopAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row {
                        Button(
                            onClick = {
                                viewModel.goToCalendarToday()
                            },
                        ) { Text("今日") }
                    }
                    Row {
                        Button(onClick = { viewModel.navigate(NavRoute.Settings.name) }, modifier = Modifier.padding(start = 8.dp)) {
                            Text("設定")
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (NavRoute.valueOf(state.currentRoute)) {
                NavRoute.Calendar -> CalendarScreen(
                    state = calendarVm.state,
                    onPrev = { calendarVm.prevMonth() },
                    onNext = { calendarVm.nextMonth() },
                    onSelect = { date -> viewModel.navigate(NavRoute.Preview.name, date) },
                )

                NavRoute.Preview -> {
                    LaunchedEffect(state.selectedDate) {
                        state.selectedDate?.let { previewVm.load(it) }
                    }
                    PreviewScreen(
                        state = previewVm.state,
                        onBack = { viewModel.navigate(NavRoute.Calendar.name) },
                        onEdit = { viewModel.navigate(NavRoute.Edit.name, state.selectedDate) },
                    )
                }

                NavRoute.Edit -> {
                    LaunchedEffect(state.selectedDate) {
                        state.selectedDate?.let { editVm.load(it) }
                    }
                    EditScreen(
                        state = editVm.state,
                        onContentChange = {
                            editVm.updateContent(it)
                        },
                        onSave = { editVm.save() },
                    )
                }

                NavRoute.Settings -> SettingsScreen(
                    settingsVm,
                    onSaved = { viewModel.goToCalendarToday() },
                )
            }
            if (state.isLoading) {
                Text("Loading...")
            }
            state.errorMessage?.let { Text("Error: $it") }
        }
    }
}
