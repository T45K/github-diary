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
     * add null to head and bottom as calendar layout from Sun to Sat
     */
    fun weeks(): List<List<CalendarDay?>> {
        val dayOfWeek = yearMonth.firstDay.dayOfWeek // isoDayNumber, 1: Mon, ..., 7: Sun
        val headPadding = dayOfWeek.isoDayNumber % 7 // 0: Sun, ..., 6: Sat
        val bottomPadding = (35 - yearMonth.lastDay.day - headPadding) % 7

        return (List(headPadding) { null } + days + List(bottomPadding) { null }).chunked(7)
    }
}

typealias CalendarDay = Pair<LocalDate, HasContent>
typealias HasContent = Boolean
