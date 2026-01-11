package io.github.t45k.githubDiary.calendar

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.isoDayNumber

data class Calendar(val yearMonth: YearMonth, val days: List<CalendarDay>) {
    companion object {
        suspend fun init(yearMonth: YearMonth, checkContent: suspend (LocalDate) -> HasContent): Calendar {
            val days = coroutineScope {
                (yearMonth.firstDay..yearMonth.lastDay)
                    .map { day -> async { day to checkContent(day) } }
                    .awaitAll()
            }

            return Calendar(yearMonth, days)
        }
    }

    /**
     * add null to head and bottom to format calendar from Sun to Sat
     */
    fun weeks(): List<List<CalendarDay?>> {
        val dayOfWeek = yearMonth.firstDay.dayOfWeek // isoDayNumber is 1: Mon, ..., 7: Sun
        val offset = dayOfWeek.isoDayNumber % 7 // 0: Sun, ..., 6: Sat
        val lastPadding = (35 - yearMonth.lastDay.day - offset) % 7

        return (List(offset) { null } + days + List(lastPadding) { null }).chunked(7)
    }
}

typealias CalendarDay = Pair<LocalDate, HasContent>
typealias HasContent = Boolean
