package core.time

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DateProviderTest {

    @Test
    fun `today returns date in JST timezone`() {
        val fixedInstant = Instant.parse("2026-01-02T00:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }

        val dateProvider = DateProvider(fixedClock)
        val today = dateProvider.today()

        assertEquals(LocalDate(2026, 1, 2), today)
    }

    @Test
    fun `today handles timezone boundary - late UTC becomes next day in JST`() {
        val fixedInstant = Instant.parse("2026-01-01T20:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }

        val dateProvider = DateProvider(fixedClock)
        val today = dateProvider.today()

        assertEquals(LocalDate(2026, 1, 2), today)
    }

    @Test
    fun `today handles timezone boundary - early UTC stays same day in JST`() {
        val fixedInstant = Instant.parse("2026-01-02T10:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }

        val dateProvider = DateProvider(fixedClock)
        val today = dateProvider.today()

        assertEquals(LocalDate(2026, 1, 2), today)
    }
}
