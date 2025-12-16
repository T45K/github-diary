package domain.usecase

import core.model.Result
import data.auth.TokenValidator

class ValidateTokenUseCase(
    private val tokenValidator: TokenValidator
) {
    suspend operator fun invoke(token: String): Result<Boolean> {
        return tokenValidator.validate(token)
    }
}