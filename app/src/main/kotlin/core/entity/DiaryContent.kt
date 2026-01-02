package core.entity

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.char

/**
 * @param content should be start with "# YYYY/MM/DD (Day)"
 */
data class DiaryContent(val date: LocalDate, val content: String) {
    companion object {
        fun init(date: LocalDate): DiaryContent = DiaryContent(date, "# ${date.format(diaryTitleDateFormat)}")

        private val diaryTitleDateFormat = LocalDate.Format {
            year()
            char('/')
            monthNumber()
            char('/')
            day()
            char(' ')
            char('(')
            dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
            char(')')
        }
    }

    fun updateContent(content: String): DiaryContent {
        val diaryHeader = "# ${date.format(diaryTitleDateFormat)}"
        return if (content.startsWith(diaryHeader)) {
            DiaryContent(date, content)
        } else {
            DiaryContent(date, "$diaryHeader\n\n$content")
        }
    }
}
