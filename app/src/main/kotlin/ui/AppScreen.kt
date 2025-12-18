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
import core.time.DateProvider
import data.auth.TokenStore
import data.auth.TokenValidator
import data.github.GitHubClient
import data.repo.DiaryRepository
import data.repo.SettingsRepository
import domain.usecase.FetchDiaryUseCase
import domain.usecase.FetchMonthDiariesUseCase
import domain.usecase.ValidateTokenUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val tokenStore = remember { TokenStore() }
    val settingsRepo = remember { SettingsRepository() }
    val tokenProvider = remember {
        { tokenStore.load().let { if (it is core.model.Result.Success) it.value?.token else null } }
    }
    val httpClient = remember { GitHubClient.create(tokenProvider) }
    val dateProvider = remember { DateProvider() }
    val diaryRepo = remember { DiaryRepository(data.github.ContentsApi(httpClient)) }
    val fetchMonthUC = remember { FetchMonthDiariesUseCase(diaryRepo, dateProvider) }
    val fetchDiaryUC = remember { FetchDiaryUseCase(diaryRepo) }
    val settingsVm = remember { SettingsViewModel(tokenStore, settingsRepo, ValidateTokenUseCase(TokenValidator(httpClient))) }
    val calendarVm = remember { CalendarViewModel(fetchMonthUC, dateProvider.today().year, dateProvider.today().monthValue) }
    val previewVm = remember { PreviewViewModel(fetchDiaryUC, state.selectedDate ?: dateProvider.today()) }
    val editVm = remember { EditViewModel(diaryRepo) }

    val syncCurrent: () -> Unit = {
        val ownerRepo = parseOwnerRepo(settingsVm.state.repo)
        ownerRepo?.let { (owner, repo) ->
            when (ui.navigation.NavRoute.valueOf(state.currentRoute)) {
                ui.navigation.NavRoute.Calendar -> viewModelScopeLaunch(calendarVm) { calendarVm.load(owner, repo) }
                ui.navigation.NavRoute.Preview -> state.selectedDate?.let { date -> viewModelScopeLaunch(previewVm) { previewVm.load(owner, repo, date) } }
                ui.navigation.NavRoute.Edit -> state.selectedDate?.let { date ->
                    editVm.setContext(owner, repo, date)
                    viewModelScopeLaunch(editVm) { editVm.loadExisting() }
                }

                ui.navigation.NavRoute.Settings -> {}
            }
        }
    }

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
                        Button(onClick = syncCurrent) { Text("Sync") }
                        Button(onClick = { viewModel.navigate(NavRoute.Settings.name) }, modifier = Modifier.padding(start = 8.dp)) {
                            Text("設定")
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (ui.navigation.NavRoute.valueOf(state.currentRoute)) {
                ui.navigation.NavRoute.Calendar -> CalendarScreen(
                    state = calendarVm.state,
                    onPrev = { calendarVm.prevMonth(); syncCurrent() },
                    onNext = { calendarVm.nextMonth(); syncCurrent() },
                    onSelect = {
                        viewModel.navigate(ui.navigation.NavRoute.Preview.name, it)
                    },
                )

                ui.navigation.NavRoute.Preview -> PreviewScreen(
                    state = previewVm.state,
                    onBack = { viewModel.navigate(ui.navigation.NavRoute.Calendar.name) },
                    onEdit = { viewModel.navigate(ui.navigation.NavRoute.Edit.name, state.selectedDate) },
                )

                ui.navigation.NavRoute.Edit -> {
                    parseOwnerRepo(settingsVm.state.repo)?.let { (owner, repo) ->
                        state.selectedDate?.let { date -> editVm.setContext(owner, repo, date) }
                    }
                    EditScreen(
                        state = editVm.state,
                        onContentChange = { editVm.updateContent(it) },
                        onSave = { editVm.save() },
                    )
                }

                ui.navigation.NavRoute.Settings -> SettingsScreen(
                    settingsVm,
                    onSaved = {
                        viewModel.goToCalendarToday()
                        syncCurrent()
                    },
                )
            }
            if (state.isLoading) {
                Text("Loading...")
            }
            state.errorMessage?.let { Text("Error: $it") }
        }
    }

    LaunchedEffect(state.currentRoute, settingsVm.state.repo, state.selectedDate) {
        syncCurrent()
    }
}

private fun viewModelScopeLaunch(target: Any, block: suspend () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch { block() }
}

private fun parseOwnerRepo(text: String): Pair<String, String>? {
    val trimmed = text.trim()
    if (!trimmed.contains("/")) return null
    val parts = trimmed.split("/")
    if (parts.size != 2 || parts.any { it.isBlank() }) return null
    return parts[0] to parts[1]
}
