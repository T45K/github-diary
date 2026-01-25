package io.github.t45k.githubDiary.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
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
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppScreen() {
    val dateProvider: DateProvider = koinInject()
    val backStack = remember { mutableStateListOf<NavRoute>(NavRoute.Calendar(dateProvider.currentYearMonth())) }
    val calendarRevisionByMonth = remember { mutableStateMapOf<String, Int>() }

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
                        onClick = {
                            backStack.clear()
                            backStack.add(NavRoute.Calendar(dateProvider.currentYearMonth()))
                        },
                    ) { Text("今日") }

                    Button(
                        onClick = { backStack.add(NavRoute.Settings) },
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
                onBack = { backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<NavRoute.Calendar> { key ->
                        val yearMonth = key.yearMonth
                        val calendarRevision = calendarRevisionByMonth[yearMonth.toString()] ?: 0
                        val calendarViewModel: CalendarViewModel = koinViewModel(key = "${yearMonth}_$calendarRevision") { parametersOf(yearMonth) }
                        val uiState by calendarViewModel.uiState.collectAsState()

                        val navigateToPrevMonth: () -> Unit = {
                            val prevMonth = yearMonth.minusMonth()
                            backStack.clear()
                            backStack.add(NavRoute.Calendar(prevMonth))
                        }
                        val navigateToNextMonth: () -> Unit = {
                            val nextMonth = yearMonth.plusMonth()
                            backStack.clear()
                            backStack.add(NavRoute.Calendar(nextMonth))
                        }

                        SwipeNavigationContainer(
                            onSwipeBack = navigateToPrevMonth,
                            onSwipeForward = navigateToNextMonth,
                            swipeThreshold = 50f,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            CalendarScreen(
                                uiState = uiState,
                                onPrev = navigateToPrevMonth,
                                onNext = navigateToNextMonth,
                                onSelect = { date -> backStack.add(NavRoute.DiaryPreview(date)) },
                                onGoalPreview = { yearMonth -> backStack.add(NavRoute.GoalPreview(yearMonth)) },
                            )
                        }
                    }

                    entry<NavRoute.GoalPreview> { key ->
                        val yearMonth = key.yearMonth
                        val goalPreviewViewModel: GoalPreviewViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                        val uiState by goalPreviewViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            GoalPreviewScreen(
                                uiState = uiState,
                                onBack = { backStack.removeLastOrNull() },
                                onEdit = { backStack.add(NavRoute.GoalEdit(yearMonth)) },
                            )
                        }
                    }

                    entry<NavRoute.GoalEdit> { key ->
                        val yearMonth = key.yearMonth
                        val viewModel: GoalEditViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                        val uiState by viewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            GoalEditScreen(
                                uiState,
                                goBack = { backStack.removeLastOrNull() },
                                completeGoal = { viewModel.completeGoal(it) },
                                incompleteGoal = { viewModel.incompleteGoal(it) },
                                updateGoalContent = { index, goal -> viewModel.updateGoal(goal, index) },
                                removeGoal = { viewModel.removeGoal(it) },
                                addGoal = viewModel::addGoal,
                                syncLastMoney = viewModel::syncLast,
                                updateLastMoney = viewModel::updateLast,
                                updateFrontMoney = viewModel::updateFront,
                                updateBackMoney = viewModel::updateBack,
                                save = {
                                    viewModel.save {
                                        val key = yearMonth.toString()
                                        calendarRevisionByMonth[key] = (calendarRevisionByMonth[key] ?: 0) + 1
                                        backStack.clear()
                                        backStack.add(NavRoute.Calendar(yearMonth))
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
                            onSwipeBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            PreviewScreen(
                                uiState = uiState,
                                onBack = { backStack.removeLastOrNull() },
                                onEdit = { backStack.add(NavRoute.DiaryEdit(date)) },
                            )
                        }
                    }

                    entry<NavRoute.DiaryEdit> { key ->
                        val date = key.date
                        val editViewModel: EditViewModel = koinViewModel(key = date.toString()) { parametersOf(date) }
                        val uiState by editViewModel.uiState.collectAsState()

                        SwipeNavigationContainer(
                            onSwipeBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            EditScreen(
                                uiState = uiState,
                                onBack = { backStack.removeLastOrNull() },
                                onContentChange = { editViewModel.updateContent(it) },
                                onSave = {
                                    editViewModel.save { success, _ ->
                                        if (success) {
                                            val key = date.yearMonth.toString()
                                            calendarRevisionByMonth[key] = (calendarRevisionByMonth[key] ?: 0) + 1
                                            backStack.clear()
                                            backStack.add(NavRoute.Calendar(date.yearMonth))
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
                            onSwipeBack = { backStack.removeLastOrNull() },
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            SettingsScreen(
                                uiState = uiState,
                                onTokenChange = { settingsViewModel.updateToken(it) },
                                onRepoChange = { settingsViewModel.updateRepo(it) },
                                onSave = {
                                    settingsViewModel.save { success, _ ->
                                        if (success) {
                                            backStack.clear()
                                            backStack.add(NavRoute.Calendar(dateProvider.currentYearMonth()))
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
