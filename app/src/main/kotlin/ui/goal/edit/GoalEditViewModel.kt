package ui.goal.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.entity.GoalContent
import core.entity.MoneyInfo
import core.repository.GoalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minusMonth
import util.remove
import util.set

sealed interface GoalEditUiState {
    val yearMonth: YearMonth

    data class Loading(override val yearMonth: YearMonth) : GoalEditUiState

    data class Editing(
        val goal: GoalContent,
        val isSaving: Boolean,
    ) : GoalEditUiState {
        override val yearMonth: YearMonth = goal.yearMonth
    }

    data class Error(
        override val yearMonth: YearMonth,
        val message: String,
    ) : GoalEditUiState
}

class GoalEditViewModel(
    private val goalRepository: GoalRepository,
    yearMonth: YearMonth,
) : ViewModel() {
    private val _uiState = MutableStateFlow<GoalEditUiState>(GoalEditUiState.Loading(yearMonth))
    val uiState: StateFlow<GoalEditUiState> = _uiState.asStateFlow()

    init {
        load(yearMonth)
    }

    private fun load(yearMonth: YearMonth) {
        _uiState.value = GoalEditUiState.Loading(yearMonth)

        viewModelScope.launch {
            try {
                val goal = goalRepository.findByYearMonth(yearMonth)
                _uiState.value = GoalEditUiState.Editing(goal, isSaving = false)
            } catch (e: Exception) {
                _uiState.value = GoalEditUiState.Error(yearMonth, e.message ?: "")
            }
        }
    }

    fun updateGoal(content: String, index: Int) {
        updateGoals {
            if (index < 0 || index >= it.size) {
                return
            }
            it.set(index, content to it[index].second)
        }
    }

    fun completeGoal(index: Int) {
        updateGoals {
            if (index < 0 || index >= it.size) {
                return
            }
            it.set(index, it[index].first to true)
        }
    }

    fun incompleteGoal(index: Int) {
        updateGoals {
            if (index < 0 || index >= it.size) {
                return
            }
            it.set(index, it[index].first to false)
        }
    }

    fun addGoal() {
        updateGoals {
            it + ("" to false)
        }
    }

    fun removeGoal(index: Int) {
        updateGoals {
            if (index < 0 || index >= it.size) {
                return
            }
            it.remove(index)
        }
    }

    private inline fun updateGoals(f: (List<Pair<String, Boolean>>) -> List<Pair<String, Boolean>>) {
        val editing = (_uiState.value as? GoalEditUiState.Editing)?.takeUnless { it.isSaving } ?: return
        val goals = editing.goal.goals

        val updatedGoals = f(goals)

        _uiState.value = GoalEditUiState.Editing(editing.goal.copy(goals = updatedGoals), isSaving = false)
    }

    fun syncLast() = viewModelScope.launch {
        val prevMonth = _uiState.value.yearMonth.minusMonth()
        val lastAmount = goalRepository.findByYearMonth(prevMonth).moneyInfo.total

        updateMoney {
            MoneyInfo(lastAmount, it.front, it.back)
        }
    }

    fun updateLast(amount: Int) {
        updateMoney {
            MoneyInfo(amount, it.front, it.back)
        }
    }

    fun updateFront(amount: Int) {
        updateMoney {
            MoneyInfo(it.last, amount, it.back)
        }
    }

    fun updateBack(amount: Int) {
        updateMoney {
            MoneyInfo(it.last, it.front, amount)
        }
    }

    private inline fun updateMoney(f: (MoneyInfo) -> MoneyInfo) {
        val editing = (_uiState.value as? GoalEditUiState.Editing)?.takeUnless { it.isSaving } ?: return
        val moneyInfo = editing.goal.moneyInfo

        val updatedMoneyInfo = f(moneyInfo)

        _uiState.value = GoalEditUiState.Editing(editing.goal.copy(moneyInfo = updatedMoneyInfo), isSaving = false)
    }

    fun save(onSaveCompleted: () -> Unit) = viewModelScope.launch {
        val editing = (_uiState.value as? GoalEditUiState.Editing)?.takeUnless { it.isSaving } ?: return@launch
        _uiState.value = editing.copy(isSaving = true)

        try {
            goalRepository.save(editing.goal)
            onSaveCompleted()
        } catch (e: Exception) {
            _uiState.value = GoalEditUiState.Error(editing.yearMonth, e.message ?: "")
        }
    }
}
