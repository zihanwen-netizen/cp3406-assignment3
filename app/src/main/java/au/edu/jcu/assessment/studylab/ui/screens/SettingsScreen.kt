package au.edu.jcu.assessment.studylab.ui.screens

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.studylab.domain.Topic
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabUiState

@Composable
fun SettingsScreen(
    state: StudyLabUiState,
    onTopic: (Int) -> Unit,
    onDifficulty: (String) -> Unit,
    onLargeText: (Boolean) -> Unit,
    onClearData: () -> Unit
) {
    var confirmClear by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Choose a topic and reading preference. You can change these at any time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().selectableGroup().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Practice topic", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Topic.available.forEach { topic ->
                    PreferenceRow(
                        title = topic.name,
                        selected = state.preferences.topicId == topic.id,
                        onClick = { onTopic(topic.id) }
                    )
                }
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().selectableGroup().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Question difficulty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                listOf("easy", "medium", "hard").forEach { difficulty ->
                    PreferenceRow(
                        title = difficulty.replaceFirstChar { it.uppercase() },
                        selected = state.preferences.difficulty == difficulty,
                        onClick = { onDifficulty(difficulty) }
                    )
                }
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier.fillMaxWidth().toggleable(
                    value = state.preferences.largeText,
                    role = Role.Switch,
                    onValueChange = onLargeText
                ).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Larger text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Increase app text size for easier reading.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.preferences.largeText, onCheckedChange = null)
            }
        }
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Privacy and data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("When you start practice, StudyLab sends the selected topic and difficulty to Open Trivia DB. The service also receives standard connection data such as your IP address. StudyLab does not send your name or study-place note.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Practice history, preferences, your study-place note, and cached question sets are stored on this device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { confirmClear = true }) { Text("Clear my data") }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("StudyLab does not request location or create an account. Open Trivia DB questions are user-contributed and may not match JCU unit materials.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear StudyLab data?") },
            text = { Text("This removes saved study-place details, preferences, practice history, and cached questions from this device.") },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClearData() }) { Text("Clear data") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PreferenceRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) {}.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
