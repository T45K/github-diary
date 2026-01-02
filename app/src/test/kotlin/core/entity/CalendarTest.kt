package core.entity

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CalendarTest {

    @Test
    fun `init creates calendar with all dates in month`() = runTest {
        val yearMonth = YearMonth(2026, 1)
        val calendar = Calendar.init(yearMonth) { false }

        assertEquals(31, calendar.dates.size)
        assertEquals(LocalDate(2026, 1, 1), calendar.dates.first().first)
        assertEquals(LocalDate(2026, 1, 31), calendar.dates.last().first)
    }

    @Test
    fun `init calls checkContent for each date`() = runTest {
        val yearMonth = YearMonth(2026, 2)
        val checkedDates = mutableListOf<LocalDate>()

        Calendar.init(yearMonth) { date ->
            checkedDates.add(date)
            false
        }

        assertEquals(28, checkedDates.size)
    }

    @Test
    fun `init preserves hasContent result for each date`() = runTest {
        val yearMonth = YearMonth(2026, 1)
        val datesWithContent = setOf(
            LocalDate(2026, 1, 1),
            LocalDate(2026, 1, 15),
            LocalDate(2026, 1, 31)
        )

        val calendar = Calendar.init(yearMonth) { date ->
            date in datesWithContent
        }

        val day1 = calendar.dates.find { it.first == LocalDate(2026, 1, 1) }
        val day2 = calendar.dates.find { it.first == LocalDate(2026, 1, 2) }
        val day15 = calendar.dates.find { it.first == LocalDate(2026, 1, 15) }

        assertTrue(day1!!.second)
        assertFalse(day2!!.second)
        assertTrue(day15!!.second)
    }

    @Test
    fun `init handles leap year February`() = runTest {
        val yearMonth = YearMonth(2024, 2)
        val calendar = Calendar.init(yearMonth) { false }

        assertEquals(29, calendar.dates.size)
        assertEquals(LocalDate(2024, 2, 29), calendar.dates.last().first)
    }
}
