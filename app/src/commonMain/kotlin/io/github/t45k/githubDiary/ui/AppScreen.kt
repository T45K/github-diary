package io.github.t45k.githubDiary.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.t45k.githubDiary.calendar.CalendarRefreshEvent
import io.github.t45k.githubDiary.calendar.CalendarScreen
import io.github.t45k.githubDiary.calendar.CalendarViewModel
import io.github.t45k.githubDiary.diary.edit.EditScreen
import io.github.t45k.githubDiary.diary.edit.EditViewModel
import io.github.t45k.githubDiary.diary.preview.PreviewScreen
import io.github.t45k.githubDiary.diary.preview.PreviewViewModel
import io.github.t45k.githubDiary.monthlyNote.edit.GoalEditScreen
import io.github.t45k.githubDiary.monthlyNote.edit.GoalEditViewModel
import io.github.t45k.githubDiary.monthlyNote.preview.GoalPreviewScreen
import io.github.t45k.githubDiary.monthlyNote.preview.GoalPreviewViewModel
import io.github.t45k.githubDiary.setting.SettingsScreen
import io.github.t45k.githubDiary.setting.SettingsViewModel
import io.github.t45k.githubDiary.util.DateProvider
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppScreen(
    viewModel: AppViewModel = koinViewModel(),
) {
    val dateProvider: DateProvider = koinInject()
    val calendarRefreshEvent: CalendarRefreshEvent = koinInject()
    val backStack by viewModel.backStack.collectAsState()

    val navigateToCalendar: (YearMonth) -> Unit = { yearMonth ->
        viewModel.navigateToCalendar(yearMonth)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button(
                        onClick = { viewModel.navigateToToday() },
                    ) { Text("今日") }

                    Button(
                        onClick = { viewModel.push(NavRoute.Settings) },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text("設定")
                    }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .padding(16.dp),
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { viewModel.pop() },
                entryProvider = entryProvider {
                    entry<NavRoute.Calendar> { key ->
                        val yearMonth = key.yearMonth
                        val calendarViewModel: CalendarViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                        val uiState by calendarViewModel.uiState.collectAsState()

                        val navigateToPrevMonth: () -> Unit = { navigateToCalendar(yearMonth.minusMonth()) }
                        val navigateToNextMonth: () -> Unit = { navigateToCalendar(yearMonth.plusMonth()) }

                        val focusRequester = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        SwipeNavigationContainer(
                            onSwipeBack = navigateToPrevMonth,
                            onSwipeForward = navigateToNextMonth,
                            swipeThreshold = 50f,
                            modifier = Modifier.fillMaxSize()
                                .focusRequester(focusRequester)
                                .focusable(true)
                                .onPreviewKeyEvent { event ->
                                    val isCmdRPressed = event.type == KeyEventType.KeyDown && event.isMetaPressed && event.key == Key.R
                                    if (isCmdRPressed) {
                                        calendarRefreshEvent.requestRefresh(yearMonth)
                                        true
                                    } else {
                                        false
                                    }
                                },
                        ) {
                            CalendarScreen(
                                uiState = uiState,
                                onPrev = navigateToPrevMonth,
                                onNext = navigateToNextMonth,
                                onSelect = { date -> viewModel.push(NavRoute.DiaryPreview(date)) },
                                onGoalPreview = { yearMonth -> viewModel.push(NavRoute.GoalPreview(yearMonth)) },
                            )
                        }
                    }

                    entry<NavRoute.GoalPreview> { key ->
                        val yearMonth = key.yearMonth
                        val goalPreviewViewModel: GoalPreviewViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                        val uiState by goalPreviewViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { viewModel.pop() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            GoalPreviewScreen(
                                uiState = uiState,
                                onBack = { viewModel.pop() },
                                onEdit = { viewModel.push(NavRoute.GoalEdit(yearMonth)) },
                            )
                        }
                    }

                    entry<NavRoute.GoalEdit> { key ->
                        val yearMonth = key.yearMonth
                        val goalEditViewModel: GoalEditViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                        val uiState by goalEditViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { viewModel.pop() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            GoalEditScreen(
                                uiState,
                                goBack = { viewModel.pop() },
                                completeGoal = { goalEditViewModel.completeGoal(it) },
                                incompleteGoal = { goalEditViewModel.incompleteGoal(it) },
                                updateGoalContent = { index, goal -> goalEditViewModel.updateGoal(goal, index) },
                                removeGoal = { goalEditViewModel.removeGoal(it) },
                                addGoal = goalEditViewModel::addGoal,
                                syncLastMoney = goalEditViewModel::syncLast,
                                updateLastMoney = goalEditViewModel::updateLast,
                                updateFrontMoney = goalEditViewModel::updateFront,
                                updateBackMoney = goalEditViewModel::updateBack,
                                save = {
                                    goalEditViewModel.save {
                                        navigateToCalendar(yearMonth)
                                    }
                                },
                            )
                        }
                    }

                    entry<NavRoute.DiaryPreview> { key ->
                        val date = key.date

                        val previewViewModel: PreviewViewModel = koinViewModel(
                            key = date.toString(),
                        ) { parametersOf(date) }

                        val uiState by previewViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { viewModel.pop() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            PreviewScreen(
                                uiState = uiState,
                                onBack = { viewModel.pop() },
                                onEdit = { viewModel.push(NavRoute.DiaryEdit(date)) },
                            )
                        }
                    }

                    entry<NavRoute.DiaryEdit> { key ->
                        val date = key.date
                        val editViewModel: EditViewModel = koinViewModel(key = date.toString()) { parametersOf(date) }
                        val uiState by editViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { viewModel.pop() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            EditScreen(
                                uiState = uiState,
                                onBack = { viewModel.pop() },
                                onContentChange = { editViewModel.updateContent(it) },
                                onSave = {
                                    editViewModel.save { success, _ ->
                                        if (success) {
                                            val yearMonth = date.yearMonth
                                            calendarRefreshEvent.requestRefresh(yearMonth)
                                            navigateToCalendar(yearMonth)
                                        }
                                    }
                                },
                            )
                        }
                    }

                    entry<NavRoute.Settings> {
                        val settingsViewModel: SettingsViewModel = koinViewModel()
                        val uiState by settingsViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { viewModel.pop() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            SettingsScreen(
                                uiState = uiState,
                                onTokenChange = { settingsViewModel.updateToken(it) },
                                onRepoChange = { settingsViewModel.updateRepo(it) },
                                onSave = {
                                    settingsViewModel.save { success, _ ->
                                        if (success) {
                                            navigateToCalendar(dateProvider.currentYearMonth())
                                        }
                                    }
                                },
                            )
                        }
                    }
                },
            )
        }
    }
}
