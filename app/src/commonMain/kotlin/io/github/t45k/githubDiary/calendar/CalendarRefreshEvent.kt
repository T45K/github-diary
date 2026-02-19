package io.github.t45k.githubDiary.calendar

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class CalendarRefreshEvent {
    private val _refreshRequest = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshRequest: SharedFlow<Unit> = _refreshRequest

    fun requestRefresh() {
        _refreshRequest.tryEmit(Unit)
    }
}
