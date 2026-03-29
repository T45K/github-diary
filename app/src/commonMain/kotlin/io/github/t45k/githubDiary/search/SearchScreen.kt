package io.github.t45k.githubDiary.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.t45k.githubDiary.ui.common.LoadingBox
import kotlinx.datetime.LocalDate

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onLoadMore: () -> Unit,
    onResultClick: (LocalDate) -> Unit,
    onBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("検索") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .imePadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("検索ワードを入力") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                )
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "検索")
                }
            }

            Spacer(Modifier.height(16.dp))

            when (uiState) {
                is SearchUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("検索ワードを入力してください", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                    }
                }

                is SearchUiState.Loading -> LoadingBox()

                is SearchUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.message}", color = MaterialTheme.colors.error)
                    }
                }

                is SearchUiState.Success -> {
                    if (uiState.results.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("検索結果が見つかりませんでした", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                        }
                    } else {
                        Text(
                            text = "${uiState.totalCount} 件の結果",
                            fontSize = 14.sp,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(uiState.results, key = { it.path }) { entry ->
                                SearchResultItem(
                                    entry = entry,
                                    onClick = { entry.date?.let { onResultClick(it) } },
                                )
                            }
                            if (uiState.hasMore) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Button(onClick = onLoadMore) {
                                            Text("さらに読み込む")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    entry: SearchResultEntry,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (entry.date != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = entry.path,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (entry.date != null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
        )
        entry.fragments.forEach { fragment ->
            Spacer(Modifier.height(4.dp))
            Text(
                text = fragment,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f),
            )
        }
        Spacer(Modifier.height(8.dp))
        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
    }
}
