package io.github.t45k.githubDiary.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.t45k.githubDiary.util.yearMonthSlashFormat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onGoalPreview: (YearMonth) -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        when (uiState) {
            is CalendarUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Loading...", color = MaterialTheme.colors.onSurface)
                }
            }

            is CalendarUiState.Error -> {
                CalendarHeader(
                    yearMonth = uiState.yearMonth,
                    onPrev = onPrev,
                    onNext = onNext,
                    onGoalPreview = { _ -> },
                )
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${uiState.message}",
                        color = MaterialTheme.colors.error,
                    )
                }
            }

            is CalendarUiState.Success -> {
                CalendarHeader(
                    yearMonth = uiState.yearMonth,
                    onPrev = onPrev,
                    onNext = onNext,
                    onGoalPreview = onGoalPreview,
                )
                CalendarContent(
                    yearMonth = uiState.yearMonth,
                    calendar = uiState.calendar,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    yearMonth: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onGoalPreview: (yearMonth: YearMonth) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = yearMonth.format(yearMonthSlashFormat),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onGoalPreview(yearMonth) }) { Text("ゴール") }
            Button(onClick = onPrev) { Text("<") }
            Button(onClick = onNext) { Text(">") }
        }
    }
}

@Composable
private fun CalendarContent(
    yearMonth: YearMonth,
    calendar: Calendar,
    onSelect: (LocalDate) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("日", "月", "火", "水", "木", "金", "土").forEachIndexed { index, day ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = when (index) {
                        0 -> Color(0xFFFF5252)
                        6 -> Color(0xFF448AFF)
                        else -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }

    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))

    val firstDay = yearMonth.firstDay
    val offset = if (firstDay.dayOfWeek == DayOfWeek.SUNDAY) 0 else firstDay.dayOfWeek.ordinal + 1

    val daysList = mutableListOf<CalendarDay?>()
    repeat(offset) { daysList.add(null) }
    daysList += calendar.days
    while (daysList.size % 7 != 0) {
        daysList.add(null)
    }

    val weeks = daysList.chunked(7)
    Column(modifier = Modifier.fillMaxSize()) {
        weeks.forEach { week ->
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                week.forEachIndexed { index, day ->
                    if (day == null) {
                        SpacerCell(Modifier.weight(1f).fillMaxHeight())
                    } else {
                        DayCell(
                            day = day,
                            isSunday = index == 0,
                            isSaturday = index == 6,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onSelect = onSelect,
                        )
                    }
                }
            }
            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    isSunday: Boolean,
    isSaturday: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (LocalDate) -> Unit,
) {
    Box(
        modifier = modifier.clickable { onSelect(day.first) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = day.first.day.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    isSunday -> Color(0xFFFF5252)
                    isSaturday -> Color(0xFF448AFF)
                    else -> MaterialTheme.colors.onSurface
                },
            )
            if (day.second) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "✎",
                        fontSize = 10.sp,
                        color = MaterialTheme.colors.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SpacerCell(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}
