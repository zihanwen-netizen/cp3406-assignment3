package au.edu.jcu.assessment.studylab.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.studylab.domain.Topic
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabUiState

@Composable
fun SettingsScreen(
    state: StudyLabUiState,
    onTopic: (Int) -> Unit,
    onDifficulty: (String) -> Unit,
    onLargeText: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Choose a topic and reading preference. You can change these at any time.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                modifier = Modifier.fillMaxWidth().clickable(role = Role.Switch, onClick = { onLargeText(!state.preferences.largeText) }).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Larger text", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Increase app text size for easier reading.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.preferences.largeText, onCheckedChange = onLargeText)
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("StudyLab does not request location or create an account. Preferences and practice totals are stored locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PreferenceRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(role = Role.RadioButton, onClick = onClick).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(title, style = MaterialTheme.typography.bodyLarge)
    }
}
