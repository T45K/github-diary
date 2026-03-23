package io.github.t45k.githubDiary.diary.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.LocalDate

@Preview
@Composable
fun PreviewScreenPreview(
    @PreviewParameter(PreviewUiStatePreviewProvider::class) uiState: PreviewUiState,
) {
    PreviewScreen(uiState, {}, {}, {})
}

class PreviewUiStatePreviewProvider : PreviewParameterProvider<PreviewUiState> {
    private val date = LocalDate(2026, 1, 2)
    override val values: Sequence<PreviewUiState> = sequenceOf(
        PreviewUiState.Success(
            date = date,
            content = "# 2026/01/02 (Fri)\n\nToday I worked on the GitHub Diary project.",
        ),
        PreviewUiState.NotFound(date = date),
    )
}
