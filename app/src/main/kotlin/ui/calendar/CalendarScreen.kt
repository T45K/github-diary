package ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun CalendarScreen(state: CalendarState, onPrev: () -> Unit, onNext: () -> Unit, onSelect: (LocalDate) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onPrev) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Prev") }
            Text("${state.year}/${state.month.toString().padStart(2, '0')}")
            IconButton(onClick = onNext) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next") }
        }

        val byWeek = state.days.groupBy { it.date.with(DayOfWeek.SUNDAY) }
        byWeek.toSortedMap().values.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.sortedBy { it.date }.forEach { day ->
                    DayCell(day, onSelect)
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
                Icon(Icons.Default.Edit, contentDescription = "exists")
            }
        }
    }
}