package domain.usecase

import core.model.Result
import data.repo.DiaryRepository
import java.time.LocalDate

class SaveDiaryUseCase(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(owner: String, repo: String, date: LocalDate, content: String, sha: String? = null): Result<Unit> {
        return diaryRepository.saveDiary(owner, repo, date, content, sha)
    }
}