package au.edu.jcu.assessment.studylab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.studylab.domain.Topic
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabUiState

@Composable
fun HomeScreen(
    state: StudyLabUiState,
    onStartPractice: () -> Unit,
    onSaveStudyPlace: (String) -> Unit
) {
    var draftPlace by rememberSaveable(state.preferences.studyPlace) { mutableStateOf(state.preferences.studyPlace) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                androidx.compose.foundation.layout.Box(Modifier.padding(12.dp)) {
                    androidx.compose.material3.Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Column {
                Text("StudyLab", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("A small step toward stronger recall", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("YOUR NEXT STUDY BLOCK", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD7E9E1), fontWeight = FontWeight.SemiBold)
                Text("Practise retrieval,\none question at a time.", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text("A five-question session gives you a short recall check. Review each answer before moving on.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFF0F5F2))
                Button(onClick = onStartPractice, shape = RoundedCornerShape(16.dp)) {
                    androidx.compose.material3.Icon(Icons.Outlined.Bolt, contentDescription = null)
                    Spacer(Modifier.padding(4.dp))
                    Text("Start practice")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Sessions", state.attempts.size.toString(), Modifier.weight(1f))
            val total = state.attempts.sumOf { it.total }
            val correct = state.attempts.sumOf { it.correct }
            MetricCard("Recall accuracy", "${if (total == 0) 0 else correct * 100 / total}%", Modifier.weight(1f))
        }

        SectionHeading("Plan a place to study", "Save a place name manually. StudyLab does not access your location.")
        Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draftPlace,
                    onValueChange = { draftPlace = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Study place") },
                    placeholder = { Text("e.g. library quiet zone") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Saved on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { onSaveStudyPlace(draftPlace.trim()) }, enabled = draftPlace.trim().isNotEmpty()) { Text("Save plan") }
                }
                Text("No live availability is shown. Check campus notices before travelling.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionHeading("Choose a study mode", "Use a space that matches the task.")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StudyModeCard("Quiet focus", "Reading or recall", Modifier.weight(1f))
            StudyModeCard("Group table", "Explain ideas aloud", Modifier.weight(1f))
        }
        Text("Practice topic: ${Topic.available.firstOrNull { it.id == state.preferences.topicId }?.name ?: "Computer Science"}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StudyModeCard(title: String, description: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}
