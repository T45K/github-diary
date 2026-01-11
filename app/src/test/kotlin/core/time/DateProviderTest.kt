package core.time

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Test

class DateProviderTest {

    @Test
    fun `currentYearMonth returns year month in JST timezone`() {
        // given
        val fixedInstant = Instant.parse("2026-01-02T00:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        val dateProvider = DateProvider(fixedClock)

        // when
        val yearMonth = dateProvider.currentYearMonth()

        // then
        assert(yearMonth == YearMonth(2026, 1))
    }

    @Test
    fun `currentYearMonth handles timezone boundary - late UTC becomes next day in JST`() {
        // given
        val fixedInstant = Instant.parse("2025-12-31T20:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        val dateProvider = DateProvider(fixedClock)

        // when
        val yearMonth = dateProvider.currentYearMonth()

        // then
        assert(yearMonth == YearMonth(2026, 1))
    }

    @Test
    fun `currentYearMonth handles timezone boundary - early UTC stays same month in JST`() {
        // given
        val fixedInstant = Instant.parse("2026-01-02T10:00:00Z")
        val fixedClock = object : Clock {
            override fun now(): Instant = fixedInstant
        }
        val dateProvider = DateProvider(fixedClock)

        // when
        val yearMonth = dateProvider.currentYearMonth()

        // then
        assert(yearMonth == YearMonth(2026, 1))
    }
}
