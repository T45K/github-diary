package io.github.t45k.githubDiary.monthlyNote.edit

import io.github.t45k.githubDiary.monthlyNote.GoalContent
import io.github.t45k.githubDiary.monthlyNote.MoneyInfo
import kotlinx.datetime.YearMonth
import org.junit.jupiter.api.Test

class GoalEditScreenTest {

    @Test
    fun `canSave returns false while loading`() {
        val uiState = GoalEditUiState.Loading(YearMonth(2026, 4))

        assert(uiState.canSave() == false)
    }

    @Test
    fun `canSave returns true when editing and not saving`() {
        val uiState = GoalEditUiState.Editing(
            goal = GoalContent(
                yearMonth = YearMonth(2026, 4),
                goals = listOf("goal" to false),
                moneyInfo = MoneyInfo(0, 0, 0),
            ),
            isSaving = false,
        )

        assert(uiState.canSave())
    }

    @Test
    fun `canSave returns false while saving`() {
        val uiState = GoalEditUiState.Editing(
            goal = GoalContent(
                yearMonth = YearMonth(2026, 4),
                goals = emptyList(),
                moneyInfo = MoneyInfo(0, 0, 0),
            ),
            isSaving = true,
        )

        assert(uiState.canSave() == false)
    }
}