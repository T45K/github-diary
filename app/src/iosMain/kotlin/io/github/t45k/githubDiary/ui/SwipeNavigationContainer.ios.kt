package io.github.t45k.githubDiary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * iOS版のSwipeNavigationContainer。
 * iOSでは標準ジェスチャーに任せるため、単純なBoxとして動作する。
 */
@Composable
actual fun SwipeNavigationContainer(
    onSwipeBack: () -> Unit,
    onSwipeForward: () -> Unit,
    modifier: Modifier,
    swipeThreshold: Float,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}
