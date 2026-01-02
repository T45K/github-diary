package di

import core.repository.CalendarRepository
import core.repository.DiaryRepository
import core.repository.GitHubClient
import core.repository.SettingRepository
import core.time.DateProvider
import kotlinx.datetime.LocalDate
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ui.calendar.CalendarViewModel
import ui.edit.EditViewModel
import ui.preview.PreviewViewModel
import ui.settings.SettingsViewModel

val appModule = module {
    // Infrastructure
    single { GitHubClient() }
    single { DateProvider() }

    // Repositories
    single { SettingRepository(gitHubClient = get()) }
    single { CalendarRepository(client = get(), settingRepository = get()) }
    single { DiaryRepository(client = get(), settingRepository = get()) }

    // ViewModels
    single { SettingsViewModel(get()) }

    viewModel { (year: Int, month: Int) ->
        CalendarViewModel(get(), year, month)
    }

    viewModel { (date: LocalDate) ->
        PreviewViewModel(get(), date)
    }

    viewModel {
        EditViewModel(get(), get())
    }
}
