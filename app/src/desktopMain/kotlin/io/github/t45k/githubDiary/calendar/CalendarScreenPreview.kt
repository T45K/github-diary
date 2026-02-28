package io.github.t45k.githubDiary.calendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.YearMonth

@Preview
@Composable
fun CalendarScreenPreview(
    @PreviewParameter(CalendarUiStatePreviewProvider::class) uiState: CalendarUiState,
) {
    CalendarScreen(uiState, isVimiumModeEnabled = false, {}, {}, {}, {})
}

class CalendarUiStatePreviewProvider : PreviewParameterProvider<CalendarUiState> {
    val yearMonth = YearMonth(2025, 1)
    override val values: Sequence<CalendarUiState> = sequenceOf(CalendarUiState.Success(yearMonth, Calendar(yearMonth, emptyList())))
}
