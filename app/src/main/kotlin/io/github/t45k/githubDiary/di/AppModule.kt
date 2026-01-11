package io.github.t45k.githubDiary.di

import io.github.t45k.githubDiary.core.repository.CalendarRepository
import io.github.t45k.githubDiary.core.repository.DiaryRepository
import io.github.t45k.githubDiary.core.repository.GitHubClient
import io.github.t45k.githubDiary.core.repository.GoalRepository
import io.github.t45k.githubDiary.core.repository.SettingRepository
import io.github.t45k.githubDiary.core.time.DateProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import io.github.t45k.githubDiary.ui.calendar.CalendarViewModel
import io.github.t45k.githubDiary.ui.diary.edit.EditViewModel
import io.github.t45k.githubDiary.ui.diary.preview.PreviewViewModel
import io.github.t45k.githubDiary.ui.goal.edit.GoalEditViewModel
import io.github.t45k.githubDiary.ui.goal.preview.GoalPreviewViewModel
import io.github.t45k.githubDiary.ui.settings.SettingsViewModel

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
    single { _root_ide_package_.io.github.t45k.githubDiary.ui.settings.SettingsViewModel(settingRepository = get()) }

    viewModel { (year: Int, month: Int) ->
        _root_ide_package_.io.github.t45k.githubDiary.ui.calendar.CalendarViewModel(get(), year, month)
    }

    viewModel { (date: LocalDate) ->
        _root_ide_package_.io.github.t45k.githubDiary.ui.diary.preview.PreviewViewModel(get(), date)
    }

    viewModel { (date: LocalDate) ->
        _root_ide_package_.io.github.t45k.githubDiary.ui.diary.edit.EditViewModel(get(), date)
    }

    viewModel { (yearMonth: YearMonth) ->
        _root_ide_package_.io.github.t45k.githubDiary.ui.goal.preview.GoalPreviewViewModel(get(), yearMonth)
    }

    viewModel { (yearMonth: YearMonth) ->
        _root_ide_package_.io.github.t45k.githubDiary.ui.goal.edit.GoalEditViewModel(get(), yearMonth)
    }
}
