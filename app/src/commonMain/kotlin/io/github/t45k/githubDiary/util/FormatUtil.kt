package io.github.t45k.githubDiary.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.char

val localDateSlashFormat = LocalDate.Format { year(); char('/'); monthNumber(); char('/'); day() }
val yearMonthSlashFormat = YearMonth.Format { year(); char('/'); monthNumber() }
