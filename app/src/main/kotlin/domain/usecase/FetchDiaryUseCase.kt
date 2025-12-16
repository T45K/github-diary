package domain.usecase

import core.model.Result
import data.repo.DiaryRepository
import java.time.LocalDate

class FetchDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(owner: String, repo: String, date: LocalDate): Result<String?> {
        return diaryRepository.getDiary(owner, repo, date)
    }
}