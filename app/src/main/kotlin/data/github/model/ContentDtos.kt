package data.github.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentFile(
    val name: String,
    val path: String,
    val sha: String,
    val content: String? = null,
    val encoding: String? = null
)

@Serializable
data class PutContentRequest(
    val message: String,
    val content: String,
    val branch: String,
    val sha: String? = null
)

@Serializable
data class PutContentResponse(
    val content: ContentFile
)
