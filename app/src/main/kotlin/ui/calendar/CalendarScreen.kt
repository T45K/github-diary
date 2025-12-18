package ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun CalendarScreen(state: CalendarState, onPrev: () -> Unit, onNext: () -> Unit, onSelect: (LocalDate) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onPrev) { Text("<-") }
            Text("${state.year}/${state.month.toString().padStart(2, '0')}")
            Button(onClick = onNext) { Text("->") }
        }

        val firstDay = LocalDate.of(state.year, state.month, 1)
        val offset = firstDay.dayOfWeek.let { if (it == DayOfWeek.SUNDAY) 0 else it.value }
        val sortedDays = state.days.sortedBy { it.date }
        val padded: List<DayItem?> = List(offset) { null } + sortedDays
        padded.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        SpacerCell()
                    } else {
                        DayCell(day, onSelect)
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) { SpacerCell() }
                }
            }
        }

        state.error?.let { Text("Error: $it") }
    }
}

@Composable
private fun DayCell(day: DayItem, onSelect: (LocalDate) -> Unit) {
    Card(modifier = Modifier.padding(4.dp).clickable { onSelect(day.date) }) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text(day.date.dayOfMonth.toString(), modifier = Modifier.padding(end = 4.dp))
            if (day.exists) {
                Text("✎")
            }
        }
    }
}

@Composable
private fun SpacerCell() {
    Card(modifier = Modifier.padding(4.dp)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Text("")
        }
    }
}
