package core.entity

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Test

class CalendarTest {

    @Test
    fun `init creates calendar with all dates in month`() = runTest {
        // given
        val yearMonth = YearMonth(2026, 1)

        // when
        val calendar = Calendar.init(yearMonth) { false }

        // then
        assert(calendar.dates.size == 31)
        assert(calendar.dates.first().first == LocalDate(2026, 1, 1))
        assert(calendar.dates.last().first == LocalDate(2026, 1, 31))
    }

    @Test
    fun `init calls checkContent for each date`() = runTest {
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
    fun `init preserves hasContent result for each date`() = runTest {
        // given
        val yearMonth = YearMonth(2026, 1)
        val datesWithContent = setOf(
            LocalDate(2026, 1, 1),
            LocalDate(2026, 1, 15),
            LocalDate(2026, 1, 31)
        )

        // when
        val calendar = Calendar.init(yearMonth) { date ->
            date in datesWithContent
        }

        // then
        val day1 = calendar.dates.find { it.first == LocalDate(2026, 1, 1) }
        val day2 = calendar.dates.find { it.first == LocalDate(2026, 1, 2) }
        val day15 = calendar.dates.find { it.first == LocalDate(2026, 1, 15) }

        assert(day1!!.second == true)
        assert(day2!!.second == false)
        assert(day15!!.second == true)
    }

    @Test
    fun `init handles leap year February`() = runTest {
        // given
        val yearMonth = YearMonth(2024, 2)

        // when
        val calendar = Calendar.init(yearMonth) { false }

        // then
        assert(calendar.dates.size == 29)
        assert(calendar.dates.last().first == LocalDate(2024, 2, 29))
    }
}
