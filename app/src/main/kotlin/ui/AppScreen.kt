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
import core.repository.DiaryRepository
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

    val gitHubClient = core.repository.GitHubClient()
    val settingRepository = SettingRepository(gitHubClient = gitHubClient)
    val calendarRepository = CalendarRepository(gitHubClient, settingRepository)
    val diaryRepository = DiaryRepository(gitHubClient, settingRepository)
    val dateProvider = DateProvider()
    val settingsViewModel = remember { SettingsViewModel(settingRepository) }
    val calendarViewModel = remember { CalendarViewModel(calendarRepository, dateProvider.today().year, dateProvider.today().month.number) }
    val previewViewModel = remember { PreviewViewModel(diaryRepository, state.selectedDate ?: dateProvider.today()) }
    val editViewModel = remember { EditViewModel(diaryRepository, dateProvider) }

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
                    state = calendarViewModel.state,
                    onPrev = { calendarViewModel.prevMonth() },
                    onNext = { calendarViewModel.nextMonth() },
                    onSelect = { date -> viewModel.navigate(NavRoute.Preview.name, date) },
                )

                NavRoute.Preview -> {
                    LaunchedEffect(state.selectedDate) {
                        state.selectedDate?.let { previewViewModel.load(it) }
                    }
                    PreviewScreen(
                        state = previewViewModel.state,
                        onBack = { viewModel.navigate(NavRoute.Calendar.name) },
                        onEdit = { viewModel.navigate(NavRoute.Edit.name, state.selectedDate) },
                    )
                }

                NavRoute.Edit -> {
                    LaunchedEffect(state.selectedDate) {
                        state.selectedDate?.let { editViewModel.load(it) }
                    }
                    EditScreen(
                        state = editViewModel.state,
                        onContentChange = {
                            editViewModel.updateContent(it)
                        },
                        onSave = { editViewModel.save() },
                    )
                }

                NavRoute.Settings -> SettingsScreen(
                    settingsViewModel,
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
