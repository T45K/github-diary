package core.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.WeekFields
import java.util.Locale

object DateFormatter {
    private val displayFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy/MM/dd (EEE)")
        .toFormatter(Locale.ENGLISH)

    val weekFields: WeekFields = WeekFields.SUNDAY_START

    fun formatDisplay(date: LocalDate): String {
        return date.format(displayFormatter)
    }

    fun buildPath(date: LocalDate): String {
        val year = date.year.toString().padStart(4, '0')
        val month = date.monthValue.toString().padStart(2, '0')
        val day = date.dayOfMonth.toString().padStart(2, '0')
        return "$year/$month/$day/README.md"
    }

    fun firstDayOfWeek(): DayOfWeek = weekFields.firstDayOfWeek
}