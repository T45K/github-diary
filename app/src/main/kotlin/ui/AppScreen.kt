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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import core.repository.CalendarRepository
import core.repository.DiaryRepository
import core.repository.GitHubClient
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
fun AppScreen() {
    val navController = rememberNavController()
    val dateProvider = DateProvider()
    val today = dateProvider.today()

    val gitHubClient = remember { GitHubClient() }
    val settingRepository = remember { SettingRepository(gitHubClient = gitHubClient) }
    val calendarRepository = remember { CalendarRepository(gitHubClient, settingRepository) }
    val diaryRepository = remember { DiaryRepository(gitHubClient, settingRepository) }
    val settingsViewModel = remember { SettingsViewModel(settingRepository) }
    val calendarViewModel = remember { CalendarViewModel(calendarRepository, today.year, today.month.number) }

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
                                navController.navigate(NavRoute.Calendar) {
                                    popUpTo(NavRoute.Calendar) { inclusive = true }
                                }
                            },
                        ) { Text("今日") }
                    }
                    Row {
                        Button(
                            onClick = { navController.navigate(NavRoute.Settings) },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("設定")
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            NavHost(
                navController = navController,
                startDestination = NavRoute.Calendar,
            ) {
                composable<NavRoute.Calendar> {
                    CalendarScreen(
                        state = calendarViewModel.state,
                        onPrev = { calendarViewModel.prevMonth() },
                        onNext = { calendarViewModel.nextMonth() },
                        onSelect = { date -> navController.navigate(NavRoute.Preview(date)) },
                    )
                }

                composable<NavRoute.Preview> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.Preview>()
                    val date = route.toLocalDate()
                    val previewViewModel = remember(date) { PreviewViewModel(diaryRepository, date) }

                    LaunchedEffect(date) {
                        previewViewModel.load(date)
                    }

                    PreviewScreen(
                        state = previewViewModel.state,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(NavRoute.Edit(date)) },
                    )
                }

                composable<NavRoute.Edit> { backStackEntry ->
                    val route = backStackEntry.toRoute<NavRoute.Edit>()
                    val date = route.toLocalDate()
                    val editViewModel = remember(date) { EditViewModel(diaryRepository, dateProvider) }

                    LaunchedEffect(date) {
                        editViewModel.load(date)
                    }

                    EditScreen(
                        state = editViewModel.state,
                        onBack = { navController.popBackStack() },
                        onContentChange = { editViewModel.updateContent(it) },
                        onSave = {
                            editViewModel.save()
                            navController.navigate(NavRoute.Calendar) {
                                popUpTo(NavRoute.Calendar) { inclusive = true }
                            }
                        },
                    )
                }

                composable<NavRoute.Settings> {
                    SettingsScreen(
                        settingsViewModel,
                        onSaved = {
                            navController.navigate(NavRoute.Calendar) {
                                popUpTo(NavRoute.Calendar) { inclusive = true }
                            }
                        },
                    )
                }
            }
        }
    }
}
