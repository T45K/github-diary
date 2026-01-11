package io.github.t45k.githubDiary.core.entity

import arrow.core.Either
import arrow.core.left
import arrow.core.right

data class GitHubRepositoryPath(val owner: String, val name: String) {
    companion object {

        /**
         * @param path: Repository path in format "owner/name"
         */
        operator fun invoke(path: String): Either<Unit, GitHubRepositoryPath> {
            val split = path.split("/")
            return if (split.size == 2) GitHubRepositoryPath(split[0], split[1]).right()
            else Unit.left()
        }
    }

    override fun toString(): String = "$owner/$name"
}
