package ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@Composable
fun EditScreen(state: EditState, onContentChange: (String) -> Unit, onSave: () -> Unit) {
    var localText by remember(state.content) { mutableStateOf(state.content) }

    Column(Modifier.padding(16.dp)) {
        Text(state.date.format(DateTimeFormatter.ISO_DATE))
        OutlinedTextField(
            value = localText,
            onValueChange = {
                localText = it
                onContentChange(it)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(Modifier.padding(top = 12.dp)) {
            Button(onClick = onSave, enabled = !state.isSaving) {
                Text(if (state.isSaving) "Saving..." else "Save")
            }
        }

        state.error?.let {
            Text("Error: $it", modifier = Modifier.padding(top = 8.dp))
        }
    }
}
