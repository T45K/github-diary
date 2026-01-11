package io.github.t45k.githubDiary.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.t45k.githubDiary.core.time.DateProvider
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import io.github.t45k.githubDiary.ui.calendar.CalendarScreen
import io.github.t45k.githubDiary.ui.calendar.CalendarViewModel
import io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer
import io.github.t45k.githubDiary.ui.diary.edit.EditScreen
import io.github.t45k.githubDiary.ui.diary.edit.EditViewModel
import io.github.t45k.githubDiary.ui.diary.preview.PreviewScreen
import io.github.t45k.githubDiary.ui.diary.preview.PreviewViewModel
import io.github.t45k.githubDiary.ui.goal.edit.GoalEditScreen
import io.github.t45k.githubDiary.ui.goal.edit.GoalEditViewModel
import io.github.t45k.githubDiary.ui.goal.preview.GoalPreviewScreen
import io.github.t45k.githubDiary.ui.goal.preview.GoalPreviewViewModel
import io.github.t45k.githubDiary.ui.navigation.NavRoute
import io.github.t45k.githubDiary.ui.settings.SettingsScreen
import io.github.t45k.githubDiary.ui.settings.SettingsViewModel

@Composable
fun AppScreen() {
    val navController = rememberNavController()

    val dateProvider: DateProvider = koinInject()

    val settingsViewModel: io.github.t45k.githubDiary.ui.settings.SettingsViewModel = koinInject()

    Scaffold(
        topBar = {
            TopAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Button(
                        onClick = {
                            navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(dateProvider.currentYearMonth())) {
                                popUpTo<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { inclusive = true }
                            }
                        },
                    ) { Text("今日") }

                    Button(
                        onClick = { navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Settings) },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text("設定")
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            NavHost(
                navController = navController,
                startDestination = _root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(dateProvider.currentYearMonth()),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { backStackEntry ->
                    val route = backStackEntry.toRoute<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar>()
                    val yearMonth = route.yearMonth

                    val calendarViewModel: io.github.t45k.githubDiary.ui.calendar.CalendarViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth.year, yearMonth.month.number) }

                    val uiState by calendarViewModel.uiState.collectAsState()

                    val navigateToPrevMonth = {
                        val prevMonth = yearMonth.minusMonth()
                        navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(prevMonth)) {
                            popUpTo<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { inclusive = true }
                        }
                    }
                    val navigateToNextMonth = {
                        val nextMonth = yearMonth.plusMonth()
                        navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(nextMonth)) {
                            popUpTo<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { inclusive = true }
                        }
                    }

                    _root_ide_package_.io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer(
                        onSwipeBack = navigateToPrevMonth,
                        onSwipeForward = navigateToNextMonth,
                        swipeThreshold = 50f,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        _root_ide_package_.io.github.t45k.githubDiary.ui.calendar.CalendarScreen(
                            uiState = uiState,
                            onPrev = navigateToPrevMonth,
                            onNext = navigateToNextMonth,
                            onSelect = { date -> navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.DiaryPreview(date)) },
                            onGoalPreview = { yearMonth -> navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.GoalPreview(yearMonth)) },
                        )
                    }
                }

                composable<io.github.t45k.githubDiary.ui.navigation.NavRoute.GoalPreview> { backStackEntry ->
                    val route = backStackEntry.toRoute<io.github.t45k.githubDiary.ui.navigation.NavRoute.GoalPreview>()
                    val yearMonth = route.yearMonth

                    val goalPreviewViewModel: io.github.t45k.githubDiary.ui.goal.preview.GoalPreviewViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }

                    val uiState by goalPreviewViewModel.uiState.collectAsState()

                    _root_ide_package_.io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        _root_ide_package_.io.github.t45k.githubDiary.ui.goal.preview.GoalPreviewScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.GoalEdit(yearMonth)) },
                        )
                    }
                }

                composable<io.github.t45k.githubDiary.ui.navigation.NavRoute.GoalEdit> { backStackEntry ->
                    val route = backStackEntry.toRoute<io.github.t45k.githubDiary.ui.navigation.NavRoute.GoalEdit>()
                    val yearMonth = route.yearMonth

                    val viewModel: io.github.t45k.githubDiary.ui.goal.edit.GoalEditViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                    val uiState by viewModel.uiState.collectAsState()

                    _root_ide_package_.io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        _root_ide_package_.io.github.t45k.githubDiary.ui.goal.edit.GoalEditScreen(
                            uiState,
                            goBack = { navController.popBackStack() },
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
                                    navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(yearMonth)) {
                                        popUpTo<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { inclusive = true }
                                    }
                                }
                            },
                        )
                    }
                }

                composable<io.github.t45k.githubDiary.ui.navigation.NavRoute.DiaryPreview> { backStackEntry ->
                    val route = backStackEntry.toRoute<io.github.t45k.githubDiary.ui.navigation.NavRoute.DiaryPreview>()
                    val date = route.date

                    val previewViewModel: io.github.t45k.githubDiary.ui.diary.preview.PreviewViewModel = koinViewModel(
                        key = date.toString(),
                    ) { parametersOf(date) }

                    val uiState by previewViewModel.uiState.collectAsState()

                    _root_ide_package_.io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        _root_ide_package_.io.github.t45k.githubDiary.ui.diary.preview.PreviewScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.DiaryEdit(date)) },
                        )
                    }
                }

                composable<io.github.t45k.githubDiary.ui.navigation.NavRoute.DiaryEdit> { backStackEntry ->
                    val route = backStackEntry.toRoute<io.github.t45k.githubDiary.ui.navigation.NavRoute.DiaryEdit>()
                    val date = route.date

                    val editViewModel: io.github.t45k.githubDiary.ui.diary.edit.EditViewModel = koinViewModel(
                        key = date.toString(),
                    ) {
                        parametersOf(date)
                    }

                    val uiState by editViewModel.uiState.collectAsState()

                    _root_ide_package_.io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        _root_ide_package_.io.github.t45k.githubDiary.ui.diary.edit.EditScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onContentChange = { editViewModel.updateContent(it) },
                            onSave = {
                                editViewModel.save { success, _ ->
                                    if (success) {
                                        navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(date.yearMonth)) {
                                            popUpTo<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { inclusive = true }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                composable<io.github.t45k.githubDiary.ui.navigation.NavRoute.Settings> {
                    val uiState by settingsViewModel.uiState.collectAsState()

                    _root_ide_package_.io.github.t45k.githubDiary.ui.component.SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        _root_ide_package_.io.github.t45k.githubDiary.ui.settings.SettingsScreen(
                            uiState = uiState,
                            onTokenChange = { settingsViewModel.updateToken(it) },
                            onRepoChange = { settingsViewModel.updateRepo(it) },
                            onSave = {
                                settingsViewModel.save { success, _ ->
                                    if (success) {
                                        navController.navigate(_root_ide_package_.io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar(dateProvider.currentYearMonth())) {
                                            popUpTo<io.github.t45k.githubDiary.ui.navigation.NavRoute.Calendar> { inclusive = true }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
