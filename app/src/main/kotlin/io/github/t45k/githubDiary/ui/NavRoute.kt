package io.github.t45k.githubDiary.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data class Calendar(val year: Int, val month: Int) : NavRoute {
        constructor(yearMonth: YearMonth) : this(yearMonth.year, yearMonth.month.number)

        val yearMonth: YearMonth get() = YearMonth(year, month)
    }

    @Serializable
    data class DiaryPreview(val year: Int, val month: Int, val day: Int) : NavRoute {
        constructor(date: LocalDate) : this(date.year, date.month.number, date.day)

        val date: LocalDate get() = LocalDate(year, month, day)
    }

    @Serializable
    data class DiaryEdit(val year: Int, val month: Int, val day: Int) : NavRoute {
        constructor(date: LocalDate) : this(date.year, date.month.number, date.day)

        val date: LocalDate get() = LocalDate(year, month, day)
    }

    @Serializable
    data class GoalPreview(val year: Int, val month: Int) : NavRoute {
        constructor(yearMonth: YearMonth) : this(yearMonth.year, yearMonth.month.number)

        val yearMonth: YearMonth get() = YearMonth(year, month)
    }

    @Serializable
    data class GoalEdit(val year: Int, val month: Int) : NavRoute {
        constructor(yearMonth: YearMonth) : this(yearMonth.year, yearMonth.month.number)

        val yearMonth: YearMonth get() = YearMonth(year, month)
    }

    @Serializable
    data object Settings : NavRoute
}