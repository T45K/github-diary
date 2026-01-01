package core.time

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DateProvider(
    private val clock: Clock = Clock.System,
) {
    fun today(): LocalDate = clock.todayIn(TimeZone.of("Asia/Tokyo"))
}
