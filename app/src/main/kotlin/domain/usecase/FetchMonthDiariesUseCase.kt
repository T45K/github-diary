package domain.usecase

import core.model.Result
import core.time.DateProvider
import data.repo.DiaryRepository
import java.time.LocalDate

open class FetchMonthDiariesUseCase(
    private val diaryRepository: DiaryRepository,
    private val dateProvider: DateProvider
) {
    data class DayStatus(val date: LocalDate, val exists: Boolean)

    open suspend operator fun invoke(owner: String, repo: String, year: Int? = null, month: Int? = null): Result<List<DayStatus>> {
        val today = dateProvider.today()
        val targetYear = year ?: today.year
        val targetMonth = month ?: today.monthValue
        val first = LocalDate.of(targetYear, targetMonth, 1)
        val last = first.plusMonths(1).minusDays(1)

        val statuses = mutableListOf<DayStatus>()
        var cursor = first
        while (!cursor.isAfter(last)) {
            when (val existsResult = diaryRepository.exists(owner = owner, repo = repo, date = cursor)) {
                is Result.Success -> statuses.add(DayStatus(cursor, existsResult.value))
                is Result.Failure -> return existsResult
            }
            cursor = cursor.plusDays(1)
        }
        return Result.Success(statuses)
    }
}