package io.github.t45k.githubDiary.calendar

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

data class Calendar(val dates: List<Pair<LocalDate, HasContent>>) {
    companion object {
        suspend fun init(yearMonth: YearMonth, checkContent: suspend (LocalDate) -> HasContent): Calendar {
            val dates = coroutineScope {
                (yearMonth.firstDay..yearMonth.lastDay)
                    .map { day -> async { day to checkContent(day) } }
                    .awaitAll()
            }

            return Calendar(dates)
        }
    }
}

typealias HasContent = Boolean
