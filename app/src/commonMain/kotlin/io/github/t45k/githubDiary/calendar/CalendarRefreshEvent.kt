package io.github.t45k.githubDiary.calendar

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.YearMonth

class CalendarRefreshEvent {
    private val _refreshRequest = MutableSharedFlow<YearMonth>(extraBufferCapacity = 1)
    val refreshRequest: SharedFlow<YearMonth> = _refreshRequest

    fun requestRefresh(yearMonth: YearMonth) {
        _refreshRequest.tryEmit(yearMonth)
    }
}
