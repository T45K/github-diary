package domain.usecase

import core.model.Result
import data.repo.DiaryRepository
import java.time.LocalDate

open class FetchDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {
    open suspend operator fun invoke(owner: String, repo: String, date: LocalDate): Result<String?> {
        return diaryRepository.getDiary(owner, repo, date)
    }
}
