package io.github.t45k.githubDiary.calendar

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Test

class CalendarTest {

    @Test
    suspend fun `init creates calendar with all dates in month`() {
        // given
        val yearMonth = YearMonth(2026, 1)

        // when
        val calendar = Calendar.init(yearMonth) { false }

        // then
        assert(calendar.days.size == 31)
        assert(calendar.days.first().first == LocalDate(2026, 1, 1))
        assert(calendar.days.last().first == LocalDate(2026, 1, 31))
    }

    @Test
    suspend fun `init calls checkContent for each date`() {
        // given
        val yearMonth = YearMonth(2026, 2)
        val checkedDates = mutableListOf<LocalDate>()

        // when
        Calendar.init(yearMonth) { date ->
            checkedDates.add(date)
            false
        }

        // then
        assert(checkedDates.size == 28)
    }

    @Test
    suspend fun `init preserves hasContent result for each date`() {
        // given
        val yearMonth = YearMonth(2026, 1)
        val datesWithContent = setOf(
            LocalDate(2026, 1, 1),
            LocalDate(2026, 1, 15),
            LocalDate(2026, 1, 31),
        )

        // when
        val calendar = Calendar.init(yearMonth) { date ->
            date in datesWithContent
        }

        // then
        val day1 = calendar.days.find { it.first == LocalDate(2026, 1, 1) }
        val day2 = calendar.days.find { it.first == LocalDate(2026, 1, 2) }
        val day15 = calendar.days.find { it.first == LocalDate(2026, 1, 15) }

        assert(day1!!.second == true)
        assert(day2!!.second == false)
        assert(day15!!.second == true)
    }

    @Test
    suspend fun `init handles leap year February`() {
        // given
        val yearMonth = YearMonth(2024, 2)

        // when
        val calendar = Calendar.init(yearMonth) { false }

        // then
        assert(calendar.days.size == 29)
        assert(calendar.days.last().first == LocalDate(2024, 2, 29))
    }

    @Test
    suspend fun `weeks returns calendar layout with null padding`() {
        listOf(
            // null on both head and bottom
            YearMonth(2025, 12) to listOf(
                listOf(null, 1, 2, 3, 4, 5, 6),
                listOf(7, 8, 9, 10, 11, 12, 13),
                listOf(14, 15, 16, 17, 18, 19, 20),
                listOf(21, 22, 23, 24, 25, 26, 27),
                listOf(28, 29, 30, 31, null, null, null),
            ),
            // null on head
            YearMonth(2026, 1) to listOf(
                listOf(null, null, null, null, 1, 2, 3),
                listOf(4, 5, 6, 7, 8, 9, 10),
                listOf(11, 12, 13, 14, 15, 16, 17),
                listOf(18, 19, 20, 21, 22, 23, 24),
                listOf(25, 26, 27, 28, 29, 30, 31),
            ),
            // null on bottom
            YearMonth(2026, 3) to listOf(
                listOf(1, 2, 3, 4, 5, 6, 7),
                listOf(8, 9, 10, 11, 12, 13, 14),
                listOf(15, 16, 17, 18, 19, 20, 21),
                listOf(22, 23, 24, 25, 26, 27, 28),
                listOf(29, 30, 31, null, null, null, null),
            ),
            // no null
            YearMonth(2026, 2) to listOf(
                listOf(1, 2, 3, 4, 5, 6, 7),
                listOf(8, 9, 10, 11, 12, 13, 14),
                listOf(15, 16, 17, 18, 19, 20, 21),
                listOf(22, 23, 24, 25, 26, 27, 28),
            ),
        ).forEach { (yearMonth, expected) ->
            // when
            val weeks = Calendar.init(yearMonth) { false }.weeks()

            // then:
            val daysOfWeeks = weeks.map { week -> week.map { it?.first?.day } }
            assert(daysOfWeeks == expected)
        }
    }
}
