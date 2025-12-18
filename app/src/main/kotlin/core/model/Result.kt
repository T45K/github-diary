package core.model

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val message: String? = null, val cause: Throwable? = null) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> cause
    }
}
