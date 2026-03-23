package io.github.t45k.githubDiary.setting

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Preview
@Composable
fun SettingScreenPreview(
    @PreviewParameter(SettingsUiStatePreviewProvider::class) uiState: SettingsUiState,
) {
    SettingsScreen(uiState, {}, {}, {})
}

class SettingsUiStatePreviewProvider : PreviewParameterProvider<SettingsUiState> {
    override val values: Sequence<SettingsUiState> = sequenceOf(
        SettingsUiState.Ready(
            token = "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
            repo = "owner/repo",
        ),
    )
}
