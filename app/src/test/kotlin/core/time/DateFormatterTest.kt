package core.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateFormatterTest {

    @Test
    fun `format display uses yyyy MM dd with weekday`() {
        val date = LocalDate.of(2024, 1, 7) // Sunday
        val formatted = DateFormatter.formatDisplay(date)

        assertEquals("2024/01/07 (Sun)", formatted)
    }

    @Test
    fun `build path produces yyyy MM dd README path`() {
        val date = LocalDate.of(2024, 12, 31)

        assertEquals("2024/12/31/README.md", DateFormatter.buildPath(date))
    }

    @Test
    fun `week starts on sunday`() {
        assertEquals(DayOfWeek.SUNDAY, DateFormatter.firstDayOfWeek())
    }

    @Test
    fun `date provider uses configured timezone`() {
        val clock = Clock.fixed(Instant.parse("2024-01-01T15:30:00Z"), ZoneId.of("Asia/Tokyo"))
        val provider = DateProvider(clock)

        assertEquals(LocalDate.of(2024, 1, 2), provider.today())
    }
}
