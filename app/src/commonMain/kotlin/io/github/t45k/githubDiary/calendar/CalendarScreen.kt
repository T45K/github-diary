package io.github.t45k.githubDiary.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.t45k.githubDiary.ui.common.LoadingBox
import io.github.t45k.githubDiary.util.toVimiumChar
import io.github.t45k.githubDiary.util.yearMonthSlashFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format

@Composable
fun CalendarScreen(
    uiState: CalendarUiState,
    isVimiumModeEnabled: Boolean,
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
            is CalendarUiState.Loading -> LoadingBox()

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
                    calendar = uiState.calendar,
                    isVimiumModeEnabled,
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
            Button(onClick = { onGoalPreview(yearMonth) }) { Text("Goal") }
            Button(onClick = onPrev) { Text("<") }
            Button(onClick = onNext) { Text(">") }
        }
    }
}

@Composable
private fun CalendarContent(
    calendar: Calendar,
    isVimiumModeEnabled: Boolean,
    onSelect: (LocalDate) -> Unit,
) {
    DaysOfWeek()
    Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
    CalendarDays(calendar, isVimiumModeEnabled, onSelect)
}

@Composable
private fun DaysOfWeek() {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEachIndexed { index, day ->
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
}

@Composable
private fun CalendarDays(calendar: Calendar, isVimiumModeEnabled: Boolean, onSelect: (LocalDate) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        calendar.weeks().forEach { week ->
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                week.forEachIndexed { index, day ->
                    if (day == null) {
                        EmptyDayCell(Modifier.weight(1f).fillMaxHeight())
                    } else {
                        DayCell(
                            day = day,
                            isSunday = index == 0,
                            isSaturday = index == 6,
                            isVimiumModeEnabled,
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
    isVimiumModeEnabled: Boolean,
    modifier: Modifier = Modifier,
    onSelect: (LocalDate) -> Unit,
) {
    val (localDate, hasContent) = day

    Box(
        modifier = modifier.clickable { onSelect(localDate) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = localDate.day.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        isSunday -> Color(0xFFFF5252)
                        isSaturday -> Color(0xFF448AFF)
                        else -> MaterialTheme.colors.onSurface
                    },
                )
                if (isVimiumModeEnabled) {
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Yellow),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = localDate.toVimiumChar(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black,
                                style = TextStyle()
                            )
                        }
                    }
                } else Spacer(Modifier.weight(1f))
            }
            if (hasContent) {
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
private fun EmptyDayCell(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}
