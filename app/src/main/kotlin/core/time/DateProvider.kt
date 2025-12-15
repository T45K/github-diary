package core.time

import core.AppConfig
import java.time.Clock
import java.time.LocalDate

class DateProvider(
    private val clock: Clock = Clock.system(AppConfig.defaultZoneId)
) {
    fun today(): LocalDate = LocalDate.now(clock)
}