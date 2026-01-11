package core.entity

import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Test

class GoalContentTest {
    @Test
    fun `parse returns object when appropriate format is given`() {
        // given
        val content = """
            # 2023/01
            
            ## Goals
            - [ ] foo
            - [x] bar
            - [X] baz
            
            ## Money
            - Last: 1,000
            - Front: 100
            - Back: 950
            - Total: 1,050
            - Diff: 50
        """.trimIndent()

        // when
        val goalContent = GoalContent.parse(content)

        // then
        assert(goalContent.yearMonth == YearMonth(2023, 1))
        assert(goalContent.goals == listOf("foo" to false, "bar" to true, "baz" to true))
        assert(goalContent.moneyInfo == MoneyInfo(1000, 100, 950, 1050, 50))
    }

    @Test
    fun `content return formatted string`() {
        // given
        val goalContent = GoalContent(
            yearMonth = YearMonth(2023, 1),
            goals = listOf("foo" to false, "bar" to true, "baz" to true),
            moneyInfo = MoneyInfo(1000, 100, 950, 1050, 50),
        )

        // when
        val content = goalContent.content()

        // then
        val expected = """
            # 2023/01

            ## Goals
            - [ ] foo
            - [x] bar
            - [x] baz

            ## Money
            - Last: 1,000
            - Front: 100
            - Back: 950
            - Total: 1,050
            - Diff: 50
            
        """.trimIndent()
        assert(content == expected)
    }

    @Test
    fun `parse and content can be combined`() {
        // given:
        val content = """
            # 2023/01
            
            ## Goals
            - [ ] foo
            - [x] bar
            - [x] baz
            
            ## Money
            - Last: 1,000
            - Front: 100
            - Back: 950
            - Total: 1,050
            - Diff: 50
            
        """.trimIndent()

        // when
        val content2 = GoalContent.parse(content).content()

        // then
        assert(content == content2)
    }
}
