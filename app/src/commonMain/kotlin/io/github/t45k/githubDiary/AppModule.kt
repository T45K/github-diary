package io.github.t45k.githubDiary

import io.github.t45k.githubDiary.calendar.CalendarRefreshEvent
import io.github.t45k.githubDiary.calendar.CalendarRepository
import io.github.t45k.githubDiary.calendar.CalendarViewModel
import io.github.t45k.githubDiary.diary.DiaryRepository
import io.github.t45k.githubDiary.diary.edit.EditViewModel
import io.github.t45k.githubDiary.diary.preview.PreviewViewModel
import io.github.t45k.githubDiary.github.GitHubClient
import io.github.t45k.githubDiary.monthlyNote.GoalRepository
import io.github.t45k.githubDiary.monthlyNote.edit.GoalEditViewModel
import io.github.t45k.githubDiary.monthlyNote.preview.GoalPreviewViewModel
import io.github.t45k.githubDiary.setting.SettingRepository
import io.github.t45k.githubDiary.setting.SettingsViewModel
import io.github.t45k.githubDiary.util.DateProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Infrastructure
    single { GitHubClient() }
    single { DateProvider() }

    // Events
    single { CalendarRefreshEvent() }

    // Repositories
    single { SettingRepository(gitHubClient = get()) }
    single { CalendarRepository(client = get(), settingRepository = get()) }
    single { DiaryRepository(client = get(), settingRepository = get()) }
    single { GoalRepository(client = get(), settingRepository = get()) }

    // ViewModels
    single { SettingsViewModel(settingRepository = get()) }

    viewModel { (yearMonth: YearMonth) ->
        CalendarViewModel(get(), get(), yearMonth)
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
