package io.github.t45k.githubDiary.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

/**
 * 二本指スワイプジェスチャーでナビゲーションを行うコンテナ。
 * macOSトラックパッドの水平スクロールを検出して、戻る/進むアクションを発火する。
 *
 * @param onSwipeBack 左から右へのスワイプで呼ばれる（戻る）
 * @param onSwipeForward 右から左へのスワイプで呼ばれる（進む）
 * @param modifier Modifier
 * @param swipeThreshold スワイプを発火するまでの累積スクロール量
 * @param content コンテンツ
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SwipeNavigationContainer(
    onSwipeBack: (() -> Unit)? = null,
    onSwipeForward: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 100f,
    content: @Composable () -> Unit
) {
    var accumulatedScroll by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .onPointerEvent(PointerEventType.Scroll, PointerEventPass.Initial) { event ->
                val scrollDelta = event.changes.firstOrNull()?.scrollDelta?.x ?: 0f

                // 水平スクロールを累積
                accumulatedScroll += scrollDelta

                // しきい値を超えたらアクション発火
                // 左から右スワイプ（scrollDelta.x < 0）→ 戻る
                // 右から左スワイプ（scrollDelta.x > 0）→ 進む
                when {
                    accumulatedScroll < -swipeThreshold && onSwipeBack != null -> {
                        onSwipeBack()
                        accumulatedScroll = 0f
                    }
                    accumulatedScroll > swipeThreshold && onSwipeForward != null -> {
                        onSwipeForward()
                        accumulatedScroll = 0f
                    }
                }
            }
            .onPointerEvent(PointerEventType.Exit, PointerEventPass.Initial) {
                // マウスが領域外に出たらリセット
                accumulatedScroll = 0f
            }
    ) {
        content()
    }
}
