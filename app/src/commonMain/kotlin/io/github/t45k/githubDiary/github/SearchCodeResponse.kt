package io.github.t45k.githubDiary.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchCodeResponse(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("incomplete_results") val incompleteResults: Boolean,
    val items: List<SearchCodeItem>,
)

@Serializable
data class SearchCodeItem(
    val name: String,
    val path: String,
    val sha: String,
    @SerialName("text_matches") val textMatches: List<TextMatch> = emptyList(),
)

@Serializable
data class TextMatch(
    val fragment: String,
    val matches: List<TextMatchDetail> = emptyList(),
)

@Serializable
data class TextMatchDetail(
    val text: String,
    val indices: List<Int> = emptyList(),
)
