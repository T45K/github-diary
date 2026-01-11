package ui

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
import core.time.DateProvider
import kotlinx.datetime.minusMonth
import kotlinx.datetime.number
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ui.calendar.CalendarScreen
import ui.calendar.CalendarViewModel
import ui.component.SwipeNavigationContainer
import ui.diary.edit.EditScreen
import ui.diary.edit.EditViewModel
import ui.diary.preview.PreviewScreen
import ui.diary.preview.PreviewViewModel
import ui.goal.edit.GoalEditScreen
import ui.goal.edit.GoalEditViewModel
import ui.goal.preview.GoalPreviewScreen
import ui.goal.preview.GoalPreviewViewModel
import ui.navigation.NavRoute
import ui.settings.SettingsScreen
import ui.settings.SettingsViewModel

@Composable
fun AppScreen() {
    val navController = rememberNavController()

    val dateProvider: DateProvider = koinInject()

    val settingsViewModel: SettingsViewModel = koinInject()

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
                            navController.navigate(NavRoute.Calendar(dateProvider.currentYearMonth())) {
                                popUpTo<NavRoute.Calendar> { inclusive = true }
                            }
                        },
                    ) { Text("今日") }

                    Button(
                        onClick = { navController.navigate(NavRoute.Settings) },
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
                startDestination = NavRoute.Calendar(dateProvider.currentYearMonth()),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable<NavRoute.Calendar> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.Calendar>()
                    val yearMonth = route.yearMonth

                    val calendarViewModel: CalendarViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth.year, yearMonth.month.number) }

                    val uiState by calendarViewModel.uiState.collectAsState()

                    val navigateToPrevMonth = {
                        val prevMonth = yearMonth.minusMonth()
                        navController.navigate(NavRoute.Calendar(prevMonth)) {
                            popUpTo<NavRoute.Calendar> { inclusive = true }
                        }
                    }
                    val navigateToNextMonth = {
                        val nextMonth = yearMonth.plusMonth()
                        navController.navigate(NavRoute.Calendar(nextMonth)) {
                            popUpTo<NavRoute.Calendar> { inclusive = true }
                        }
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
                            onSelect = { date -> navController.navigate(NavRoute.DiaryPreview(date)) },
                            onGoalPreview = { yearMonth -> navController.navigate(NavRoute.GoalPreview(yearMonth)) },
                        )
                    }
                }

                composable<NavRoute.GoalPreview> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.GoalPreview>()
                    val yearMonth = route.yearMonth

                    val goalPreviewViewModel: GoalPreviewViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }

                    val uiState by goalPreviewViewModel.uiState.collectAsState()

                    SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        GoalPreviewScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate(NavRoute.GoalEdit(yearMonth)) },
                        )
                    }
                }

                composable<NavRoute.GoalEdit> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.GoalEdit>()
                    val yearMonth = route.yearMonth

                    val viewModel: GoalEditViewModel = koinViewModel(key = yearMonth.toString()) { parametersOf(yearMonth) }
                    val uiState by viewModel.uiState.collectAsState()

                    SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        GoalEditScreen(
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
                                    navController.navigate(NavRoute.Calendar(yearMonth)) {
                                        popUpTo<NavRoute.Calendar> { inclusive = true }
                                    }
                                }
                            },
                        )
                    }
                }

                composable<NavRoute.DiaryPreview> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.DiaryPreview>()
                    val date = route.date

                    val previewViewModel: PreviewViewModel = koinViewModel(
                        key = date.toString(),
                    ) { parametersOf(date) }

                    val uiState by previewViewModel.uiState.collectAsState()

                    SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        PreviewScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate(NavRoute.DiaryEdit(date)) },
                        )
                    }
                }

                composable<NavRoute.DiaryEdit> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.DiaryEdit>()
                    val date = route.date

                    val editViewModel: EditViewModel = koinViewModel(
                        key = date.toString(),
                    ) {
                        parametersOf(date)
                    }

                    val uiState by editViewModel.uiState.collectAsState()

                    SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        EditScreen(
                            uiState = uiState,
                            onBack = { navController.popBackStack() },
                            onContentChange = { editViewModel.updateContent(it) },
                            onSave = {
                                editViewModel.save { success, _ ->
                                    if (success) {
                                        navController.navigate(NavRoute.Calendar(date.yearMonth)) {
                                            popUpTo<NavRoute.Calendar> { inclusive = true }
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                composable<NavRoute.Settings> {
                    val uiState by settingsViewModel.uiState.collectAsState()

                    SwipeNavigationContainer(
                        onSwipeBack = { navController.popBackStack() },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SettingsScreen(
                            uiState = uiState,
                            onTokenChange = { settingsViewModel.updateToken(it) },
                            onRepoChange = { settingsViewModel.updateRepo(it) },
                            onSave = {
                                settingsViewModel.save { success, _ ->
                                    if (success) {
                                        navController.navigate(NavRoute.Calendar(dateProvider.currentYearMonth())) {
                                            popUpTo<NavRoute.Calendar> { inclusive = true }
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
