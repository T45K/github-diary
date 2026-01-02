package ui.navigation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoute {
    @Serializable
    data object Calendar : NavRoute

    @Serializable
    data class Preview(val year: Int, val month: Int, val day: Int) : NavRoute {
        constructor(date: LocalDate) : this(date.year, date.month.number, date.day)
        fun toLocalDate(): LocalDate = LocalDate(year, month, day)
    }

    @Serializable
    data class Edit(val year: Int, val month: Int, val day: Int) : NavRoute {
        constructor(date: LocalDate) : this(date.year, date.month.number, date.day)
        fun toLocalDate(): LocalDate = LocalDate(year, month, day)
    }

    @Serializable
    data object Settings : NavRoute
}
