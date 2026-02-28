package io.github.t45k.githubDiary.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun SwipeNavigationContainer(
    onSwipeBack: () -> Unit,
    onSwipeForward: () -> Unit,
    modifier: Modifier,
    swipeThreshold: Float,
    content: @Composable () -> Unit,
) {
    var accumulatedScroll by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .onPointerEvent(PointerEventType.Scroll, PointerEventPass.Initial) { event: PointerEvent ->
                val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.x ?: 0f

                // 水平スクロールを累積
                accumulatedScroll += scrollDelta

                // しきい値を超えたらアクション発火
                // 左から右スワイプ（scrollDelta.x < 0）→ 戻る
                // 右から左スワイプ（scrollDelta.x > 0）→ 進む
                when {
                    accumulatedScroll < -swipeThreshold -> {
                        onSwipeBack()
                        accumulatedScroll = 0f
                    }

                    accumulatedScroll > swipeThreshold -> {
                        onSwipeForward()
                        accumulatedScroll = 0f
                    }
                }
            }
            .onPointerEvent(PointerEventType.Exit, PointerEventPass.Initial) { _: PointerEvent ->
                // マウスが領域外に出たらリセット
                accumulatedScroll = 0f
            },
    ) {
        content()
    }
}
