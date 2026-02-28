package io.github.t45k.githubDiary.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 二本指スワイプジェスチャーでナビゲーションを行うコンテナ。
 * macOSトラックパッドの水平スクロールを検出して、戻る/進むアクションを発火する。
 * Androidでは単純なBoxとして動作する（スワイプ機能なし）。
 *
 * @param onSwipeBack 左から右へのスワイプで呼ばれる（戻る）
 * @param onSwipeForward 右から左へのスワイプで呼ばれる（進む）
 * @param modifier Modifier
 * @param swipeThreshold スワイプを発火するまでの累積スクロール量
 * @param content コンテンツ
 */
@Composable
expect fun SwipeNavigationContainer(
    onSwipeBack: () -> Unit = {},
    onSwipeForward: () -> Unit = {},
    modifier: Modifier = Modifier,
    swipeThreshold: Float = 80f,
    content: @Composable () -> Unit,
)
