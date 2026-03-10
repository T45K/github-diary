package io.github.t45k.githubDiary.diary.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format

@Composable
fun ReadMoreScreen(
    uiState: ReadMoreUiState,
    onBack: () -> Unit,
    onLoadPrevious: () -> Unit,
    onLoadNext: () -> Unit,
) {
    val listState = rememberLazyListState()
    var hasScrolledToInitialDate by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.entries) {
        val initialIndex = uiState.entries.indexOfFirst { it.date == uiState.initialDate }
        if (!hasScrolledToInitialDate && initialIndex >= 0) {
            listState.scrollToItem(initialIndex)
            hasScrolledToInitialDate = true
        }
    }

    LaunchedEffect(listState, uiState.entries) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            when {
                firstVisibleIndex != null && firstVisibleIndex <= 1 -> ScrollEdge.Top
                lastVisibleIndex != null && lastVisibleIndex >= uiState.entries.lastIndex - 1 -> ScrollEdge.Bottom
                else -> null
            }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { edge ->
                when (edge) {
                    ScrollEdge.Top -> onLoadPrevious()
                    ScrollEdge.Bottom -> onLoadNext()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("まとめて読む") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (uiState.isLoadingPrevious) {
                item(key = "loading_previous") {
                    LoadingMoreBox()
                }
            }

            items(items = uiState.entries, key = { it.date.toString() }) { entry ->
                ReadMoreDiaryCard(entry)
            }

            if (uiState.isLoadingNext) {
                item(key = "loading_next") {
                    LoadingMoreBox()
                }
            }
        }
    }
}

@Composable
private fun ReadMoreDiaryCard(entry: ReadMoreDiaryEntryUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(entry.date.format(LocalDate.Formats.ISO), style = MaterialTheme.typography.h6)
            Spacer(Modifier.height(8.dp))
            when (entry) {
                is ReadMoreDiaryEntryUiState.Success -> Text(entry.content)
                is ReadMoreDiaryEntryUiState.NotFound -> Text("この日の日記はありません")
                is ReadMoreDiaryEntryUiState.Error -> Text("Error: ${entry.message}", color = MaterialTheme.colors.error)
            }
        }
    }
}

@Composable
private fun LoadingMoreBox() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

private enum class ScrollEdge {
    Top,
    Bottom,
}