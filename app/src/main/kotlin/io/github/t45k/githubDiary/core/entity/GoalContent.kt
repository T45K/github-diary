package io.github.t45k.githubDiary.core.entity

import arrow.core.raise.nullable
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format
import kotlinx.datetime.format.char

/**
 * format:
 * ```
 * # yyyy/MM
 * 
 * ## Goals
 * - [ ] goal_1
 * - [x] goal_2
 * 
 * ## Money
 * - Last: 1,000
 * - Front: 100
 * - Back: 950
 * - Total: 1,050
 * - Diff: 50
 * ```
 */
data class GoalContent(
    val yearMonth: YearMonth,
    val goals: List<Pair<String, IsCompleted>>,
    val moneyInfo: MoneyInfo,
) {
    companion object {
        fun init(yearMonth: YearMonth): GoalContent = GoalContent(
            yearMonth,
            goals = emptyList(),
            moneyInfo = MoneyInfo(last = 0),
        )

        fun parse(content: String): GoalContent {
            val yearEnd = findYearMonth(content) ?: throw IllegalArgumentException("Year end not found in content")

            return GoalContent(
                yearMonth = yearEnd,
                goals = findGoals(content),
                moneyInfo = MoneyInfo(
                    findMoneyInfo(content, "Last") ?: 0,
                    findMoneyInfo(content, "Front") ?: 0,
                    findMoneyInfo(content, "Back") ?: 0,
                    findMoneyInfo(content, "Total") ?: 0,
                    findMoneyInfo(content, "Diff") ?: 0,
                ),
            )
        }

        private fun findYearMonth(content: String): YearMonth? {
            val regex = Regex("""^# (\d{4})/(\d{2})$""", RegexOption.MULTILINE)
            return regex.find(content)?.groupValues?.let { (_, year, month) ->
                nullable {
                    YearMonth(year.toIntOrNull().bind(), month.toIntOrNull().bind())
                }
            }
        }

        private fun findGoals(content: String): List<Pair<String, IsCompleted>> {
            val regex = Regex("""^-\s\[([ xX])]\s(.+)$""", RegexOption.MULTILINE)

            return regex.findAll(content)
                .map { it.groupValues[2] to it.groupValues[1].isNotBlank() }
                .toList()
        }

        private fun findMoneyInfo(content: String, target: String): Int? {
            val regex = Regex("""^-\s$target:\s([\d,]+)$""", RegexOption.MULTILINE)
            return regex.find(content)?.groupValues?.let { (_, value) ->
                nullable {
                    value.replace(",", "").toIntOrNull().bind()
                }
            }
        }
    }

    fun content(): String = """# ${yearMonth.format(YearMonth.Format { year(); char('/'); monthNumber() })}

## Goals
${goals.joinToString("\n") { (goal, isCompleted) -> "- [${if (isCompleted) "x" else " "}] $goal" }}

## Money
${moneyInfo.content()}
"""
}

typealias IsCompleted = Boolean

data class MoneyInfo(
    val last: Int,
    val front: Int,
    val back: Int,
    val total: Int,
    val diff: Int,
) {
    constructor(last: Int) : this(
        last,
        front = 0,
        back = 0,
        total = 0,
        diff = last,
    )

    constructor(last: Int, front: Int, back: Int) : this(
        last, front, back, total = front + back, diff = front + back - last,
    )

    fun content(): String = """
        - Last: ${last.amountFormat()}
        - Front: ${front.amountFormat()}
        - Back: ${back.amountFormat()}
        - Total: ${total.amountFormat()}
        - Diff: ${diff.amountFormat()}
    """.trimIndent()
}

private fun Int.amountFormat(): String = toString().reversed().chunked(3).joinToString(",").reversed()
