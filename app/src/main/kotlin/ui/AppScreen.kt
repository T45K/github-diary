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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import core.time.DateProvider
import data.auth.TokenStore
import data.auth.TokenValidator
import data.github.ContentsApi
import data.repo.DiaryRepository
import domain.usecase.FetchDiaryUseCase
import domain.usecase.FetchMonthDiariesUseCase
import domain.usecase.SaveDiaryUseCase
import domain.usecase.ValidateTokenUseCase
import io.ktor.client.HttpClient
import ui.calendar.CalendarScreen
import ui.calendar.CalendarViewModel
import ui.edit.EditScreen
import ui.edit.EditViewModel
import ui.preview.PreviewScreen
import ui.preview.PreviewViewModel
import ui.settings.SettingsScreen
import ui.settings.SettingsViewModel
import ui.navigation.NavRoute

@Composable
fun AppScreen(viewModel: AppViewModel, onSync: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    // シンプルなremember生成（本実装ではDIへ置換予定）
    val httpClient = remember { HttpClient() }
    val dateProvider = remember { DateProvider() }
    val diaryRepo = remember { DiaryRepository(ContentsApi(httpClient)) }
    val fetchMonthUC = remember { FetchMonthDiariesUseCase(diaryRepo, dateProvider) }
    val fetchDiaryUC = remember { FetchDiaryUseCase(diaryRepo) }
    val saveDiaryUC = remember { SaveDiaryUseCase(diaryRepo) }
    val settingsVm = remember { SettingsViewModel(TokenStore(), ValidateTokenUseCase(TokenValidator(httpClient))) }
    val calendarVm = remember { CalendarViewModel(fetchMonthUC, dateProvider.today().year, dateProvider.today().monthValue) }
    val previewVm = remember { PreviewViewModel(fetchDiaryUC, "", "", state.selectedDate ?: dateProvider.today()) }
    val editVm = remember { EditViewModel(diaryRepo, "", "", state.selectedDate ?: dateProvider.today()) }

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
            when (ui.navigation.NavRoute.valueOf(state.currentRoute)) {
                ui.navigation.NavRoute.Calendar -> CalendarScreen(
                    state = calendarVm.state,
                    onPrev = { calendarVm.prevMonth() },
                    onNext = { calendarVm.nextMonth() },
                    onSelect = { viewModel.navigate(ui.navigation.NavRoute.Preview.name, it) }
                )
                ui.navigation.NavRoute.Preview -> PreviewScreen(
                    state = previewVm.state,
                    onBack = { viewModel.navigate(ui.navigation.NavRoute.Calendar.name) },
                    onEdit = { viewModel.navigate(ui.navigation.NavRoute.Edit.name, state.selectedDate) }
                )
                ui.navigation.NavRoute.Edit -> EditScreen(
                    state = editVm.state,
                    onContentChange = { editVm.updateContent(it) },
                    onSave = { editVm.save() }
                )
                ui.navigation.NavRoute.Settings -> SettingsScreen(settingsVm)
            }
            if (state.isLoading) {
                Text("Loading...")
            }
            state.errorMessage?.let { Text("Error: $it") }
        }
    }
}