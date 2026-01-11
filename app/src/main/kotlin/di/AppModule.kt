package di

import core.repository.CalendarRepository
import core.repository.DiaryRepository
import core.repository.GitHubClient
import core.repository.GoalRepository
import core.repository.SettingRepository
import core.time.DateProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ui.calendar.CalendarViewModel
import ui.diary.edit.EditViewModel
import ui.diary.preview.PreviewViewModel
import ui.goal.edit.GoalEditViewModel
import ui.goal.preview.GoalPreviewViewModel
import ui.settings.SettingsViewModel

val appModule = module {
    // Infrastructure
    single { GitHubClient() }
    single { DateProvider() }

    // Repositories
    single { SettingRepository(gitHubClient = get()) }
    single { CalendarRepository(client = get(), settingRepository = get()) }
    single { DiaryRepository(client = get(), settingRepository = get()) }
    single { GoalRepository(client = get(), settingRepository = get()) }

    // ViewModels
    single { SettingsViewModel(settingRepository = get()) }

    viewModel { (year: Int, month: Int) ->
        CalendarViewModel(get(), year, month)
    }

    viewModel { (date: LocalDate) ->
        PreviewViewModel(get(), date)
    }

    viewModel { (date: LocalDate) ->
        EditViewModel(get(), date)
    }

    viewModel { (yearMonth: YearMonth) ->
        GoalPreviewViewModel(get(), yearMonth)
    }

    viewModel { (yearMonth: YearMonth) ->
        GoalEditViewModel(get(), yearMonth)
    }
}
