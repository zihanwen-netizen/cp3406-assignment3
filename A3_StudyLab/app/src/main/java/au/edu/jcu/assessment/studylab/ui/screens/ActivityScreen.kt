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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import au.edu.jcu.assessment.studylab.domain.Topic
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabUiState

@Composable
fun ActivityScreen(
    state: StudyLabUiState,
    onStart: () -> Unit,
    onSelect: (String) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onTryAgain: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Practice", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Retrieve what you know, then check the answer.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        when {
            state.sessionSummary != null -> {
                val summary = state.sessionSummary
                Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        androidx.compose.material3.Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Session complete", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${summary.correct} of ${summary.total} correct", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text("${summary.accuracy}% recall accuracy", style = MaterialTheme.typography.bodyLarge)
                        Text("This score describes this short practice set; it is not a measure of overall ability.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onTryAgain, shape = RoundedCornerShape(14.dp)) { Text("Try another set") }
                        TextButton(onClick = onDone) { Text("Back to home") }
                    }
                }
            }
            state.loading -> {
                Card(shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CircularProgressIndicator()
                        Text("Loading practice questions…", style = MaterialTheme.typography.bodyLarge)
                        Text("Questions are retrieved when you start a session.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            state.error != null -> {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Could not load questions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(state.error, style = MaterialTheme.typography.bodyMedium)
                        Button(onClick = onTryAgain, shape = RoundedCornerShape(14.dp)) { Text("Try again") }
                    }
                }
            }
            state.currentQuestion != null -> {
                val question = requireNotNull(state.currentQuestion)
                val progress = (state.currentIndex + 1).toFloat() / state.questions.size.coerceAtLeast(1)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("QUESTION ${state.currentIndex + 1} OF ${state.questions.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(question.difficulty.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(question.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                        Text(question.prompt, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        question.answers.forEachIndexed { index, answer ->
                            val isSelected = state.selectedAnswer == answer
                            val isCorrect = state.answerChecked && answer == question.correctAnswer
                            val background = when {
                                isCorrect -> MaterialTheme.colorScheme.primaryContainer
                                state.answerChecked && isSelected -> MaterialTheme.colorScheme.errorContainer
                                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable(
                                    enabled = !state.answerChecked,
                                    role = Role.RadioButton,
                                    onClick = { onSelect(answer) }
                                ),
                                shape = RoundedCornerShape(16.dp),
                                color = background
                            ) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = { onSelect(answer) }, enabled = !state.answerChecked)
                                    Text(answer, modifier = Modifier.weight(1f).padding(vertical = 10.dp), style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                        if (state.answerChecked) {
                            Text(
                                text = if (state.lastAnswerCorrect == true) "Correct. Take a moment to explain why this answer fits." else "Not quite. The correct answer is: ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (state.lastAnswerCorrect == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (!state.answerChecked) {
                    Button(onClick = onCheck, enabled = state.selectedAnswer != null, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                        Text("Check answer")
                    }
                } else {
                    Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                        Text(if (state.currentIndex == state.questions.lastIndex) "Finish session" else "Next question")
                    }
                }
                Text("Open Trivia DB questions · CC BY-SA 4.0 · Check unit materials for assessed content.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Ready for a recall check?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("StudyLab retrieves five multiple-choice questions from your selected topic. Your results are saved on this device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Topic: ${Topic.available.firstOrNull { it.id == state.preferences.topicId }?.name ?: "Computer Science"}", style = MaterialTheme.typography.labelLarge)
                        Text("Difficulty: ${state.preferences.difficulty.replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.labelLarge)
                        Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                            androidx.compose.material3.Icon(Icons.Outlined.Refresh, contentDescription = null)
                            Spacer(Modifier.padding(4.dp))
                            Text("Load questions")
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
