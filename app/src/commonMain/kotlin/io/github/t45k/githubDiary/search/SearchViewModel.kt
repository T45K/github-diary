package io.github.t45k.githubDiary.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun search() {
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val result = searchRepository.search(currentQuery)
                _uiState.value = SearchUiState.Success(
                    query = currentQuery,
                    results = result.items,
                    totalCount = result.totalCount,
                    currentPage = 1,
                    hasMore = result.hasMore,
                )
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState !is SearchUiState.Success || !currentState.hasMore) return

        val nextPage = currentState.currentPage + 1
        viewModelScope.launch {
            try {
                val result = searchRepository.search(currentState.query, page = nextPage)
                _uiState.value = currentState.copy(
                    results = currentState.results + result.items,
                    currentPage = nextPage,
                    hasMore = result.hasMore,
                )
            } catch (_: Exception) {
                // 既存の結果を保持しつつ追加読み込みを停止
                _uiState.value = currentState.copy(hasMore = false)
            }
        }
    }
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(
        val query: String,
        val results: List<SearchResultEntry>,
        val totalCount: Int,
        val currentPage: Int,
        val hasMore: Boolean,
    ) : SearchUiState

    data class Error(val message: String) : SearchUiState
}
