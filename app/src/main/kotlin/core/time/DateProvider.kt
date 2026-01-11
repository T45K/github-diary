package core.time

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth

class DateProvider(
    private val clock: Clock = Clock.System,
) {
    fun currentYearMonth(): YearMonth = clock.todayIn(TimeZone.of("Asia/Tokyo")).yearMonth
}
